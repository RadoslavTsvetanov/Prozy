package webserver
import types.Config
import cats.effect.*
import org.http4s.*
import org.http4s.client.*
import org.http4s.dsl.io.*
import org.http4s.ember.client.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.middleware.*
import types.Nullish
import java.util.Optional
import javax.net.ssl.SSLContext
import scala.compiletime.*
import scala.concurrent.duration.*
import scala.quoted.*
/*
* Example Config foe quick reference
* {
*  middlewares: [
*   {
*     content: """
*       def resolve(req: Request, res: Response): idk_for_now = {
*     }
*     """
*   }
*  ],
*   redirection_handler: {
*   "content": """
*     def jijojijo(req: Request): String | Null= {
*       return "http://localhost:4000"
*     }
*     """
*   }
* }
* */




def getRedirect(req: Request[IO], handler: Redirection_handler): Nullish[String] = {
  executeCustomHandler(handler.content,req)
}

object ProxyServer extends IOApp {

  val clientResource: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO]
    .withTimeout(30.seconds)
    .withIdleTimeInPool(30.seconds)
    .build

  def createHttpApp(config: Config): HttpApp[IO] = {
    val proxyRoutes = HttpRoutes.of[IO] {
      case req =>
        clientResource.use { client =>
          val rerdirectUrl = getRedirect(req, config.redirection_handler) match {
            case Null => {
              
            }
          }
          val targetUri = Uri.unsafeFromString()
          val forwardedRequest = req.withUri(targetUri)
          client.run(forwardedRequest).use { response =>
            IO.pure(response.copy(
              headers = response.headers
            ))
          }
        }
    }

    val corsConfig = CORSConfig.default
      .withAllowedOrigins(origin => config.cors.allowedOrigins.contains("*") || config.cors.allowedOrigins.contains(origin))
      .withAllowedMethods(Some(config.cors.allowedMethods.toSet))
      .withAllowedHeaders(Some(config.cors.allowedHeaders.toSet))
      .withAllowCredentials(true)
      .withMaxAge(config.cors.maxAge.seconds)

    CORS(proxyRoutes.orNotFound, corsConfig)
  }

  def createSSLContext(config: SSL_Config): IO[SSLContext] = {
    IO.pure(SSLContext.getDefault) // Placeholder
  }

  def createServer(config: Config): Resource[IO, Unit] = {
    val baseBuilder = EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port.toInt)
      .withIdleTimeout(config.timeout.millis)
      .withHttpApp(createHttpApp(config))

    config.ssl match {
      case sslConf if sslConf.isPresent =>
        Resource.eval(createSSLContext(sslConf.get())).flatMap { sslContext =>
          baseBuilder
            .withTLS(sslContext)
            .build
        }
      case _ =>
        baseBuilder.build
    }
  }

  override def run(config: Config): IO[ExitCode] = {
    createServer(config).use(_ => IO.never).as(ExitCode.Success)
  }
}
