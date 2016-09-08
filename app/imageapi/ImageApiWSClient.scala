package imageapi


import org.asynchttpclient.{DefaultAsyncHttpClient, AsyncCompletionHandler, Response, AsyncHttpClient}

import scala.concurrent.{Promise, Future}

/**
  * Created by unoedx on 08/09/16.
  */
object ImageApiWSClient {
  def get(url:String):Future[Response] = {
    val client = new DefaultAsyncHttpClient
    val builder = client.prepareGet(url)
    builder.setRequestTimeout(10000)

    val result = Promise[Response]()

    client.executeRequest(builder.build(), new AsyncCompletionHandler[Response](){
      override def onCompleted(response: Response) = {
        result.success(response)
        response
      }

      override def onThrowable(t: Throwable) = {
        result.failure(t)
      }
    })

    result.future
  }
}
