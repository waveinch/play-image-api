Play Image API Plugin
=================================

This plugins includes some basic functionality for image processing in play to be used via API (cropping, resizing,...) the transformation is done on the fly and cached (with no expire date) to S3

Configuration example
===========

```
#more info on: https://github.com/Kaliber/play-s3
aws.accessKeyId=AmazonAccessKeyId
aws.secretKey=AmazonSecretKey

s3.region="eu-west-1
s3.hostedZoneId="..."

mongodb.uri = "..."

imageapi {
    s3 {
        baseUrl="http://yourbucket.s3-eu-west-1.amazonaws.com/",
        bucket="your-domain.com"
    }
    mongo {
        cacheCollection="images-cache"
    }
}
```


Usage
===========

Controller
===

```
class MyApiImages @Inject()(
                       val reactiveMongoApi: ReactiveMongoApi,
                       val wsClient: WSClient,
                       val conf: Configuration,
                       @Named(ImageCacher.name) val imageCacherActor: ActorRef
                     )(implicit val ec:ExecutionContext) extends ApiImages {

                // source data folder
                override val baseUrl:String = "http://media.ticinoweekend.ch/"
}
```

Routes
===
```
GET		/media/width/:width/*path			        controllers.MyApiImages.width(width:Int,path)
GET		/media/cover/:width/:height/*path			controllers.MyApiImages.cover(width:Int,height:Int,path)
GET		/media/fit/:width/:height/*path			    controllers.MyApiImages.fit(width:Int,height:Int,path,color:Option[String])
```
