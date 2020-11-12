package imageapi

import javax.inject.Inject
import fly.play.s3.BucketFile
import fly.play.s3.S3
import akka.actor.Actor
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

import reactivemongo.play.json.compat._,
json2bson.{ toDocumentReader, toDocumentWriter }

/**
  * Created by unoedx on 08/09/16.
  */
object ImageCacher {

  final val name = "imageCacherActor"

  trait Factory {
    def apply(): Actor
  }

  case class Image(bytes: Array[Byte], request: String, name: String)

  case class Request(base:String, image:String,process: Process) {
    def request = {
      def operator = process match {
        case ImageCacher.Width(w) => "width-" + w
        case ImageCacher.Cover(w, h) => "cover-" + w + "-" + h
        case ImageCacher.Fit(w, h, c) => "fit-" + c.getOrElse("white") + "-" + w + "-" + h
      }
      Seq(operator, image).mkString("-")
    }
  }

  trait Process
  case class Width(width: Int) extends Process
  case class Cover(width: Int,height:Int) extends Process
  case class Fit(width: Int,height:Int,color: Option[String]) extends Process

  trait Response
  case class Redirect(url:String) extends Response
  case class ImageResponse(image: Array[Byte]) extends Response

}

class ImageCacher @Inject()(
                             reactiveMongoApi: ReactiveMongoApi,
                             wsClient: WSClient,
                             conf: Configuration,
                             imageProcessing: ImageProcessing
                           )(implicit ec: ExecutionContext) extends Actor {


  val s3baseUrl = conf.getString("imageapi.s3.baseUrl").get
  val s3bucket = conf.getString("imageapi.s3.bucket").get
  val s3cacheDir = conf.getString("imageapi.s3.cacheDir").getOrElse("")
  val cacheCollection = conf.getString("imageapi.mongo.cacheCollection").get


  implicit class ImageExtended(i:ImageCacher.Image){
    def cachedName = s3cacheDir + i.name

    def s3Url = s3baseUrl + cachedName
  }

  def cache: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection](cacheCollection))

  implicit val formatter = ImageCache.formatter


  def receive = {
    case request: ImageCacher.Request => {
        val future = for{
          cache <- tryCache(request)
          response <- cache match {
            case Some(hit) => Future(ImageCacher.Redirect(hit.s3Url))
            case None => process(request)
          }
        } yield response

        val response:ImageCacher.Response = Await.result(future, 30.seconds)
        sender ! response
    }

  }

  def tryCache(r:ImageCacher.Request) = {
    for{
      collection <- cache
      hit <- collection.find(Json.obj("request" -> r.request)).one[ImageCache]
    } yield hit
  }

  def process(r:ImageCacher.Request) = {
    println("Processing:" + r.image)
    def processor = imageProcessing.apply(r.base,r.image)
    val img = r.process match {
      case ImageCacher.Width(w) => processor.width(w)
      case ImageCacher.Cover(w,h) => processor.cover(w,h)
      case ImageCacher.Fit(w,h,c) => processor.fit(w,h,c)
    }

    for{
      data <- img
      image = ImageCacher.Image(data, r.request, r.image)
      _ <- saveImageInS3(image)
      response = ImageCacher.ImageResponse(data)
    } yield response

  }

  def saveImageInS3(image: ImageCacher.Image): Future[String] = {
    for {
      collection <- cache
      _ <- S3.fromConfiguration(wsClient,conf).getBucket(s3bucket) add BucketFile(image.cachedName, "image/jpeg", image.bytes)
      _ <- collection.insert.one(ImageCache(image.request, image.s3Url))
    } yield "Uploaded new"
  }

}