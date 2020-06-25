package edu.uchicago.cs.db.subattr.cli.components

import edu.uchicago.cs.db.subattr.ir.{PIntAny, PLabelAny, PLetterAny, PSeq, PWordAny, Pattern}
import org.apache.parquet.schema.{MessageType, PrimitiveType}
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName
import org.apache.parquet.schema.Type.Repetition

object SchemaGenerator {

  def generate(pattern: Pattern, name: String): MessageType = {
    val types = pattern match {
      case seq: PSeq => {
        seq.content.map(_ match {
          case iany: PIntAny => {
            if (iany.hasHex)
              PrimitiveTypeName.INT96
            else
              PrimitiveTypeName.INT32
          }
          case _: PLetterAny | _: PLabelAny | _: PWordAny => PrimitiveTypeName.BINARY
          case _ => null
        }).filter(_ != null).zipWithIndex.map(pair => {
          new PrimitiveType(Repetition.OPTIONAL, pair._1, pair._2.toString)
        }).toArray
      }
      case _ => {
        throw new IllegalArgumentException
      }
    }

    new MessageType(name, types: _*)
  }
}
