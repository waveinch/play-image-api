package imageapi

import javax.inject.Inject

import fly.play.s3.BucketFile
import fly.play.s3.S3
import akka.actor.Actor
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._

/**
  * Created by unoedx on 08/09/16.
  */
object ImageCacher {

  final val name = "imageCacherActor"

  trait Factory {
    def apply(): Actor
  }

  case class Image(bytes: Array[Byte], request: String, name: String)


}

class ImageCacher @Inject()(
                             reactiveMongoApi: ReactiveMongoApi,
                             wsClient: WSClient,
                             conf: Configuration
                           )(implicit ec: ExecutionContext) extends Actor {


  val s3baseUrl = conf.getString("imageapi.s3.baseUrl").get
  val s3bucket = conf.getString("imageapi.s3.bucket").get
  val s3cacheDir = conf.getString("imageapi.s3.cacheDir").getOrElse("")
  val cacheCollection = conf.getString("imageapi.mongo.cacheCollection").get


  implicit class ImageExtended(i:ImageCacher.Image){
    def cachedName = s3cacheDir + i.name

    def s3Url = s3baseUrl + cachedName
  }

  def cache: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](cacheCollection))

  implicit val formatter = ImageCache.formatter


  def receive = {
    case image: ImageCacher.Image => {
      val future = for{
        collection <- cache
        hit <- collection.find(Json.obj("request" -> image.request)).one[ImageCache]
        result <- { hit match {
          case Some(_) => Future {
            Unit
          }
          case None => saveImageInS3(image)
        }}
      } yield result


      Await.ready(future, 30 seconds)

    }
  }

  def saveImageInS3(image: ImageCacher.Image): Future[String] = {
    for {
      collection <- cache
      _ <- S3.fromConfiguration(wsClient,conf).getBucket(s3bucket) add BucketFile(image.cachedName, "image/jpeg", image.bytes)
      _ <- collection.insert(ImageCache(image.request, image.s3Url))
    } yield "Uploaded new"
  }

}