package com.matete.examples.bank.utils

import org.apache.avro.Schema
import scala.io.Source



/*trait CustomToString {
  override def toString() = s"Transaction()"
}*/
case class Transaction(
    label: Option[String],
    from: Option[String],
    to:  Option[String],
    amount: Double,
    typeTransaction: String,
    message: Option[String]
)

object Transaction {
 def initSchema: Schema = {
    
    val schemaString = Source.fromResource("transaction-schema.svc").mkString
    val parser = new Schema.Parser()
    parser.parse(schemaString)
 }
}