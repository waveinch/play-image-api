package imageapi

import java.io.InputStream

import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Image, Color}
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by unoedx on 08/09/16.
  */
class ImageProcessing(baseUrl:String)(implicit ec:ExecutionContext) {


  implicit val writer = JpegWriter().withCompression(70)

  def apply(image:String) = new ImageProcessor(image)

  class ImageProcessor(image:String) {
    def fit(width: Int, height: Int, color: Option[String] = None): Future[Array[Byte]] = {
      val c = color match {
        case Some(Colors.twBlue) => Color(110, 185, 216)
        case Some(Colors.twBlueGray) => Color(167, 188, 198)
        case _ => Color.White
      }

      process { in =>
        Image.fromStream(in)
          .fit(width, height, c)
          .bytes(JpegWriter().withCompression(90))
      }
    }

    def cover(width: Int, height: Int): Future[Array[Byte]] = process{ in =>
      Image.fromStream(in).cover(width, height).bytes
    }


    def width(width: Int): Future[Array[Byte]] = process{ in =>
      Image.fromStream(in).scaleToWidth(width).bytes
    }

    private def process(action: InputStream => Array[Byte]): Future[Array[Byte]] = for {
      response <- {
        ImageApiWSClient.get(baseUrl + image)
      }
      img <- Future {
        val in = response.getResponseBodyAsStream
        val img = action(in)
        in.close()
        img
      }
    } yield img
  }

}

object Colors{
  val twBlue = "tw-blue"
  val twBlueGray = "tw-blue-gray"
}


