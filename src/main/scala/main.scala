import cats.effect.{IO, IOApp}
import org.http4s.ember.client.EmberClientBuilder
import webserver.ReverseProxy

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    EmberClientBuilder.default[IO].build.use { client =>
      ReverseProxy()
        .withResolver(req => req.uri.toString())
        .withLogging(req => println(s"Proxying request: ${req.method} ${req.uri}"))
        .build()
        .listen(port = 8080, host = "127.0.0.1", v => v, client)
        .as(())
    }
  }
}
