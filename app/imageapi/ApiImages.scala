package imageapi

import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.duration._

import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by unoedx on 08/09/16.
  */
trait ApiImages extends Controller {



  def imageCacherActor: ActorRef
  implicit def ec:ExecutionContext

  def baseUrl:String

  implicit val timeout = Timeout(60 seconds)


  private def imageAction(trasform: () => Future[Any]) = Action.async{ r =>
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

