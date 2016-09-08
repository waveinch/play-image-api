package imageapi

import play.api.libs.json.Json

/**
  * Created by unoedx on 08/09/16.
  */
case class ImageCache(
                       request: String,
                       s3Url: String
                     )


object ImageCache {
  implicit val formatter = Json.format[ImageCache]

}
