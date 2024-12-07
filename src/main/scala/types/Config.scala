package types

import org.http4s.server.middleware.CORSConfig

import java.util.Optional

case class Redirection_handler(content: String)
case class Middleware(content: String)
enum LoggingTypes {
  case NONE, CUSTOM, ALL
} 

class CORS_Config {
  val allowedOrigins: List[String] = List("*")
  val allowedMethods: List[String] = List("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
  val allowedHeaders: List[String] = List("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")
  val maxAge: Int = 1728000
}

class SSL_Config {
  
}

// TODO: oraginze in sub objects
case class Config(
                   middlewares: List[Middleware],
                   redirection_handler: Redirection_handler,
                   logging: LoggingTypes,
                   ssl: Optional[SSL_Config],
                   port: String,
                   host: String,
                   cors: CORS_Config,
                   timeout: Int = 100000,
                   default_url: String
                 )
