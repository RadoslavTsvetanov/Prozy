package utils


import java.io.FileNotFoundException
import scala.io.Source


enum ReadFileResults {
  case NOT_FOUND, UNKNOWN_ERR
}

def readFileAndReturnTheWholeFileContent(path: String): Either[String, ReadFileResults]= {
  try {
    val fileContent = new StringBuilder("")
    val source = Source.fromFile(path)
    try {
      for (line <- source.getLines()) {
        fileContent ++= line + "\n"
      }
      Left(fileContent.toString())
    } finally {
      source.close() 
    }
  } catch {
    case _: FileNotFoundException =>
      Right(ReadFileResults.NOT_FOUND)
    case ex: Exception =>
      Right(ReadFileResults.UNKNOWN_ERR)
  }
}
