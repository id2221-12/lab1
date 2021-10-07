package sparkstreaming

import java.nio.ByteBuffer
import java.util.HashMap
import org.apache.spark.sql._
import org.apache.spark.sql.streaming._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder, Decoder}
import kafka.utils.VerifiableProperties
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random
import org.apache.spark.sql.types._

case class Message(key: String, value: Int)

case class Average(key: String, var acc: Int, var value: Double) {
  def updateWith (newMessage: Message){
    value = (value * acc + newMessage.value) / (acc + 1)
    acc += 1
  }
}

object KafkaSpark {
  def main(args: Array[String]) {

    val spark = SparkSession.builder.master("local").appName("task2").getOrCreate()
    import spark.implicits._
    val inputDF = spark.readStream.format("kafka").option("kafka.bootstrap.servers", "localhost:9092").option("subscribe", "avg").option("failOnDataLoss", "false").load()

    def mappingFunc(key: String, newMessages: Iterator[Message], state: GroupState[Average]): Average = {
      var avg = state.getOption.getOrElse { Average(key, 0, 0.0) }
      newMessages.foreach { msg => avg.updateWith(msg) }
      state.update(avg)
      avg
      // avg = (acc + 1, (oldVal * acc + newVal) / (acc + 1))
    }

    val df = inputDF.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]
    val ds: Dataset[Message] = df.map {case (k,v) => v.split(",")}.map {case Array(k,v) => Message(k,v.toInt)}.as[Message]

    // val stateDstream = messages.mapWithState(StateSpec.function(mappingFunc _))
    val averages = ds.groupByKey(msg => msg.key).mapGroupsWithState(mappingFunc _)
    val writer = averages.writeStream.format("console").outputMode("update")
    val writer2 = writer.trigger(Trigger.ProcessingTime("1 second")).option("checkpointLocation", "output/")
    val query = writer2.start()
    query.awaitTermination()

  }
}

// who thought this could be a good idea

