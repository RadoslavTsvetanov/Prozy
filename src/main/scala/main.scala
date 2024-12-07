import utils.ReadFileResults.{NOT_FOUND, UNKNOWN_ERR}
import play.api.libs.json.{Format, Json}
import types.Config
import utils.{ReadFileResults, readFileAndReturnTheWholeFileContent}
import webserver.ProxyServer
implicit val personFormat: Format[Config] = Json.format[Config]



def getEnv(): Either[ ReadFileResults,String] = {
  val env = System.getenv("ENV")
  if (env == null) {
    Left(NOT_FOUND)
  } else {
    Right(env)
  }
}

def loadConfig(): Config = {
   getEnv() match {
     case Left(e) => {
       println("no env found, defaulting to read config.json'")
     }
     case Right(v) => {
       return Json.parse(v).as[Config]
     }
   }
  
   readFileAndReturnTheWholeFileContent("config.json") match {
    case Left(v) => {
      Json.parse(v).as[Config]
    }
    case Right(e) => {
      throw new Error(e.toString());
    }
  }
  
  throw new Error("neither config.json nor ENV found, exiting")
}


@main
def main(): Unit = {
  val config = loadConfig()
  ProxyServer.run(config)
}
