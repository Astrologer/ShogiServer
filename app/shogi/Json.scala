package shogi

import org.json4s.jackson.Serialization.{read, write}
import org.json4s.DefaultFormats
import shogi.Protocol._

object Json {
  implicit val formats = DefaultFormats

  def loads[T](str: String)(implicit m: Manifest[T]): T = read[T](str)
  def dumps[T <: AnyRef](obj: T): String = write(obj)
}
