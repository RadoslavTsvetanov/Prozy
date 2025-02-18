import utils.ReadFileErrors.{NOT_FOUND, UNKNOWN_ERR}
import io.circe.*
import io.circe.parser.*
import types.Config
import utils.{ReadFileErrors, readFileAndReturnTheWholeFileContent}


import java.lang

def getEnv(): Option[String] = Option(System.getenv("ENV"))

def parseConfig(jsonStr: String): Either[Throwable, Config] = {
  try {
    val parseResult: Either[ParsingFailure, Json] = parse(jsonStr)
    parseResult match {
      case Left(parsingError) =>
        throw new IllegalArgumentException(s"Invalid JSON object: ${parsingError.message}")
      case Right(json) => {
//        Right(json(Config))
        throw new InternalError("fix me")
      }
    }

  } catch {
    case e: Exception => Left(e)
  }
}

def loadConfig(): Either[Throwable, Config] = {
  getEnv() match {
    case None =>
      println("No ENV found, defaulting to config.json")
      readFileAndReturnTheWholeFileContent("config.json") match {
        case Left(value) => parseConfig(value)
        case Right(value) => {
          println("no config.json found either")
          throw new scala.Error()
        }
      }

    case Some(envValue) =>
      parseConfig(envValue)
  }
}