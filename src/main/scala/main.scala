import cats.effect.{IO, IOApp}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri
import webserver.ReverseProxy

object Main extends IOApp.Simple {
  println("running server")

  def run: IO[Unit] = {
    EmberClientBuilder.default[IO].build.use { client =>
      ReverseProxy()
        .withResolver(req => {
          println("uri->" + req.uri.path)
          Uri.unsafeFromString("http://localhost:3000").withPath(req.uri.path)
        })
        .withLogging(req => println(s"Proxying request: ${req.method} ${req.uri}"))
        .build()
        .listen(port = 8080, host = "127.0.0.1", identity, client)
        .as(())
    }
  }
}
