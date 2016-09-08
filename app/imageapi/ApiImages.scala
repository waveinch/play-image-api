package imageapi

import akka.util.Timeout
import play.api.Configuration
import reactivemongo.play.json.collection.JSONCollection
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.duration._

import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Result, Controller}
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoComponents}

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

  implicit val timeout = Timeout(60 seconds)


  def imageAction(trasform: () => Future[Any]) = Action.async{ r =>
    val response = trasform()
    response.map{
      case ImageCacher.Redirect(url) => Redirect(url)
      case ImageCacher.ImageResponse(img) => Ok(img).as("image/jpeg")
    }
  }

  def width(width: Int, image: String) = imageAction{ () =>
    imageCacherActor ? ImageCacher.Request(baseUrl,image,ImageCacher.Width(width))
  }

  def cover(width: Int, height: Int, image: String) = imageAction{ () =>
    imageCacherActor ? ImageCacher.Request(baseUrl,image,ImageCacher.Cover(width,height))
  }

  def fit(width: Int, height: Int, image: String, color:Option[String] = None) = imageAction{ () =>
    imageCacherActor ? ImageCacher.Request(baseUrl,image,ImageCacher.Fit(width,height,color))
  }

}

