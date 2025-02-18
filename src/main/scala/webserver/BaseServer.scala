package webserver

import cats.effect.{Async, IO}
import com.comcast.ip4s
import org.http4s.client.Client
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.{HttpRoutes, Request, Response, Uri}
import types.LoggingTypes.{CUSTOM, DEFAULT, NONE}
import types.{CORSConfig, LoggingTypes}
import com.comcast.ip4s.{Host, Port}


trait IMiddleware {
  def apply(routes: HttpRoutes[IO]): HttpRoutes[IO]
}

class MiddlewareLoadedFromFile extends IMiddleware {
  def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    // Logic for applying middleware
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

class ReverseProxy private () {

  private var resolver: Option[Request[IO] => String] = None
  private var middlewares: List[IMiddleware] = List()
  private var corsConfig: Option[CORSConfig] = None
  private var logging: LoggingTypes = NONE

  def withLogging(loggingHandler: (req: Request[IO]) => Unit): ReverseProxy = {
    class LoggingMiddleware extends IMiddleware {
      override def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
        HttpRoutes { request =>
          loggingHandler(request)
          routes(request)
        }
      }
    }
    this.addMiddleware(LoggingMiddleware())
    this.logging = LoggingTypes.CUSTOM
    this
  }

  def withCors(cors: CORSConfig) = {
    this.corsConfig = Some(cors)
  }

  def withResolver(resolverFunction: Request[IO] => String): ReverseProxy = {
    resolver = Some(resolverFunction)
    this
  }

  def addMiddleware(middleware: IMiddleware): ReverseProxy = {
    middlewares = middlewares :+ middleware
    this
  }

  def addCors(config: CORSConfig): ReverseProxy = {
    corsConfig = Some(config)
    this
  }

  def listen(port: Int = 8080, host: String = "0.0.0.0")(implicit F: Async[IO]): IO[Unit] = {
    resolver match {
      case Some(resolverFn) =>
        val baseRoutes = HttpRoutes.of[IO] {
          case req =>
            val targetUrl = resolverFn(req)
            val proxyRequest = Request[IO](
              method = req.method,
              uri = Uri.unsafeFromString(targetUrl),
              headers = req.headers
            )
            Client[IO].expect[Response[IO]](proxyRequest)
        }

        this.logging match {
          case NONE => Unit
          case DEFAULT => {
            class DefaultLogging extends IMiddleware {
              override def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
                HttpRoutes { request =>
                  println("req received: " + request.toString())
                  routes(request)
                }
              }
            }
            this.addMiddleware(DefaultLogging())
          }
          case CUSTOM => Unit
        }
        val wrappedRoutes = middlewares.foldLeft(baseRoutes)((routes, mw) => mw.apply(routes))

        val finalRoutes = corsConfig match {
          case Some(config) => CORS(wrappedRoutes, config.toHttp4sCorsConfig())
          case None         => wrappedRoutes
        }

        val httpApp = Router("/" -> finalRoutes).orNotFound

        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(host) match {
            case Some(value) => value
            case None => throw new Error("host " + host + " is not in a valid format")
          })
          .withPort(Port.fromInt(port) match {
            case Some(v) => v
            case None => throw new Error("port " + port + " is not valid port format")
          })
          .withHttpApp(httpApp)
          .build
          .useForever

      case None =>
        IO.raiseError(new IllegalStateException("Resolver function must be defined before calling listen."))
    }
  }
}

object ReverseProxy {
  def apply(): ReverseProxy = new ReverseProxy()

  def withLogging(loggingHandler: (req: Request[IO]) => Unit): ReverseProxy = {
    apply().withLogging(loggingHandler)
  }

  def withCors(cors: CORSConfig): ReverseProxy = {
    apply().withCors(cors)
  }

  def withResolver(resolverFunction: Request[IO] => String): ReverseProxy = {
    apply().withResolver(resolverFunction)
  }

  def addMiddleware(middleware: IMiddleware): ReverseProxy = {
    apply().addMiddleware(middleware)
  }

  def addCors(config: CORSConfig): ReverseProxy = {
    apply().addCors(config)
  }
}
