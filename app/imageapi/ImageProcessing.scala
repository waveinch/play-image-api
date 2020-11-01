package imageapi

import java.io.InputStream

import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.{Color, RGBColor, X11Colorlist}
import com.sksamuel.scrimage.nio.JpegWriter
import javax.inject.Inject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by unoedx on 08/09/16.
  */



class ImageProcessing @Inject()(
                                wsClient: WSClient
                               )(implicit ec:ExecutionContext, materializer: Materializer) {



  def apply(baseUrl:String,image:String) = new ImageProcessor(baseUrl,image)

  class ImageProcessor(baseUrl:String,image:String) {
    def fit(width: Int, height: Int, color: Option[String] = None): Future[Array[Byte]] = {
      val c:Color = color match {
        case Some(Colors.twBlue) => new RGBColor(110, 185, 216)
        case Some(Colors.twBlueGray) => new RGBColor(167, 188, 198)
        case _ => X11Colorlist.White
      }

      process { in =>
        ImmutableImage.loader().fromStream(in)
          .fit(width, height, c.toAWT)
          .bytes(new JpegWriter().withCompression(80))
      }
    }

    def cover(width: Int, height: Int): Future[Array[Byte]] = process{ in =>
      ImmutableImage.loader().fromStream(in).cover(width, height).bytes(new JpegWriter().withCompression(80))
    }


    def width(width: Int): Future[Array[Byte]] = process{ in =>
      ImmutableImage.loader().fromStream(in).scaleToWidth(width).bytes(new JpegWriter().withCompression(80))
    }

    private def process(action: InputStream => Array[Byte]): Future[Array[Byte]] = for {
      response <- {
        wsClient.url(baseUrl + image).withMethod("GET").withRequestTimeout(60.seconds).stream()
      }
      img <- Future {
        val in = response.bodyAsSource.runWith(StreamConverters.asInputStream(60.seconds))
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


