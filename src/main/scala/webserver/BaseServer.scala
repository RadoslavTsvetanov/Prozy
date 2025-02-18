package webserver

import cats.effect.{Async, ExitCode, IO}
import com.comcast.ip4s
import org.http4s.client.Client
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response, Uri}
import types.LoggingTypes.{CUSTOM, DEFAULT, NONE}
import types.{CORSConfig, LoggingTypes}
import com.comcast.ip4s.{Host, Port}


trait IMiddleware {
  def apply(routes: HttpRoutes[IO]): HttpRoutes[IO]
}

class MiddlewareLoadedFromFile extends IMiddleware {
  def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    routes
  }
}

case class Origin(value: String)

object Origin {
  def apply(origin: String): Option[Origin] = {
    if (origin.indexOf("http") < 0 && origin.indexOf("https") < 0) {
      None
    } else {
      Some(new Origin(origin))
    }
  }
}

enum Method(val method: String) {
  case Get extends Method("GET")
  case Post extends Method("POST")
  case Put extends Method("PUT")
  case Delete extends Method("DELETE")
}

class ReverseProxy private (
                             val resolver: Option[Request[IO] => String],
                             val middlewares: List[IMiddleware],
                             val corsConfig: Option[CORSConfig],
                             val logging: LoggingTypes
                           ) {

  def listen(
    port: Int = 8080,
    host: String = "127.0.0.1",
    resolver: Uri => Uri,
    client: Client[IO]
): IO[ExitCode] = {

  val proxyRoutes = HttpRoutes.of[IO] { case req =>
    client.run(req.withUri(resolver(req.uri))).use(IO.pure)
  }

  val httpApp = Router("/" -> proxyRoutes).orNotFound

  EmberServerBuilder
    .default[IO]
    .withHost(Host.fromString(host).getOrElse(sys.error(s"Invalid host: $host")))
    .withPort(Port.fromInt(port).getOrElse(sys.error(s"Invalid port: $port")))
    .withHttpApp(httpApp)
    .build
    .useForever
    .as(ExitCode.Success)
}
}

object ReverseProxy { // we are using it to have a builder
  def apply(): Builder = new Builder()

  class Builder {
    private var resolver: Option[Request[IO] => String] = None
    private var middlewares: List[IMiddleware] = List()
    private var corsConfig: Option[CORSConfig] = None
    private var logging: LoggingTypes = NONE

    def withLogging(loggingHandler: Request[IO] => Unit): Builder = {
      class LoggingMiddleware extends IMiddleware {
        override def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
          HttpRoutes { request =>
            loggingHandler(request)
            routes(request)
          }
        }
      }
      middlewares = middlewares :+ new LoggingMiddleware()
      logging = CUSTOM
      this
    }

    def withResolver(resolverFunction: Request[IO] => String): Builder = {
      resolver = Some(resolverFunction)
      this
    }

    def addMiddleware(middleware: IMiddleware): Builder = {
      middlewares = middlewares :+ middleware
      this
    }

    def addCors(config: CORSConfig): Builder = {
      corsConfig = Some(config)
      this
    }

    def build(): ReverseProxy = {
      new ReverseProxy(resolver, middlewares, corsConfig, logging)
    }
  }
}
