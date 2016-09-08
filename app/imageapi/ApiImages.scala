package imageapi

import play.api.Configuration
import reactivemongo.play.json.collection.JSONCollection
import akka.actor.ActorRef
import com.sksamuel.scrimage.Color
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Result, Controller}
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{ReactiveMongoApi, MongoController, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by unoedx on 08/09/16.
  */
trait ApiImages extends Controller with MongoController with ReactiveMongoComponents {




  def conf:Configuration


  def imageCacherActor: ActorRef
  def wsClient:WSClient

  implicit def ec: ExecutionContext

  def baseUrl:String

  val cacheCollection = conf.getString("imageapi.mongo.cacheCollection").get
  val s3baseUrl = conf.getString("imageapi.s3.baseUrl").get
  def cache = reactiveMongoApi.database.map(_.collection[JSONCollection](cacheCollection))




  implicit val writer = JpegWriter().withCompression(70)

  implicit val formatter = ImageCache.formatter

  private def cacheImage(request: String, name: String)(image: () => Future[Array[Byte]]): Future[Result] = {

    for{
      collection <- cache
      hit <- collection.find(Json.obj("request" -> request)).one[ImageCache]
      result <- { hit match {
        case Some(cachedImage) => Future(Redirect(cachedImage.s3Url))
        case None => image().map { img =>
          imageCacherActor ! ImageCacher.Image(img, request, name)
          Ok(img).as("image/jpeg")
        }
      }}
    } yield result

  }

  def width(width: Int, image: String) = Action.async { r =>
    cacheImage(r.path, "width-" + width + "-" + image) { () =>
      ImageApiWSClient.get(baseUrl + image).map { response =>
        val in = response.getResponseBodyAsStream
        Image.fromStream(in).scaleToWidth(width).bytes
      }
    }
  }

  def cover(width: Int, height: Int, image: String) = Action.async { r =>
    cacheImage(r.path, "cover-" + width + "-" + height + "-" + image) { () =>
      ImageApiWSClient.get(baseUrl + image).map { response =>
        val in = response.getResponseBodyAsStream
        Image.fromStream(in).cover(width, height).bytes
      }
    }
  }

  def fit(width: Int, height: Int, image: String, color:Option[String] = None) = Action.async { r =>

    val c = color match {
      case Some(ApiImagesColors.twBlue) => Color(110,185,216)
      case Some(ApiImagesColors.twBlueGray) => Color(167, 188, 198)
      case _ => Color.White
    }

    cacheImage(r.path+color.getOrElse("white"), "fit-"+color.getOrElse("white")+"-" + width + "-" + height + "-" + image) { () =>
      ImageApiWSClient.get(baseUrl + image).map { response =>
        val in = response.getResponseBodyAsStream
        Image.fromStream(in)
          .fit(width, height, c)
          .bytes(JpegWriter().withCompression(90))
      }
    }
  }

}


object ApiImagesColors{
  val twBlue = "tw-blue"
  val twBlueGray = "tw-blue-gray"
}