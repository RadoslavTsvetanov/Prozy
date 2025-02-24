import Service.{AUTH, MAIN, UNKNOWN}
import cats.effect.{IO, IOApp}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Request, Uri}
import webserver.ReverseProxy

enum Service(val name: String):
  case AUTH extends Service("auth")
  case MAIN extends Service("main")
  case UNKNOWN extends Service("unknown")

object Service:
  def fromString(name: String): Service = name match
    case "auth" => AUTH
    case "main" => MAIN
    case _      => UNKNOWN

def resolveService(req: Request[IO]): String = {
  val pathSegments = req.uri.path.segments.map(_.decoded())
  val service = pathSegments.headOption.map(Service.fromString).getOrElse(UNKNOWN)
  service match
    case AUTH => {
      println("redirecting req to localhost:3000")
      "http://localhost:3000"
    }
    case MAIN => "http://main-service"
    case UNKNOWN => "http://unknown-service"
}

object Main extends IOApp.Simple:
  println("running server")

  def run: IO[Unit] =
    EmberClientBuilder.default[IO].build.use { client =>
      ReverseProxy()
        .withResolver { req =>
          val baseUrl = resolveService(req) 
          Uri.unsafeFromString(baseUrl).withPath(req.uri.path)
        }
        .withLogging(req => IO.println(s"Proxying request: ${req.method} ${req.uri}"))
        .build()
        .listen(port = 8080, host = "127.0.0.1", identity, client)
        .as(())
    }
