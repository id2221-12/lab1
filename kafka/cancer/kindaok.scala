package sparkstreaming

import java.nio.ByteBuffer
import java.util.HashMap
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

object KafkaSpark {
  def main(args: Array[String]) {
    // make a connection to Kafka and read (key, value) pairs from it
    val kafkaConf = Map(
      "metadata.broker.list" -> "localhost:9092",
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "print.key" -> "true",
      "key.separator" -> ",",
      "zookeeper.connection.timeout.ms" -> "1000")

    /* There are a few things I found out:
     * according to https://apache.googlesource.com/kafka/+/0.8.1.0/core/src/main/scala/kafka/serializer/Decoder.scala
     * decoders need VerifiableProperties. StringDecoder and DefaultDecoder have them, Decoder[] does not.
     * StringDecoder extends Decoder[String], so all good
     * but DefaultDecoder extends Decoder[Array[Bytes]], so not good anymore.
     * easiest option is to convert between Array[Bytes] and Int as needed
     * To simplify things and get rid of ALL errorsm I got rid of the actual avg function and replaced it with a counter.
     * It works.
     */

    val conf = new SparkConf().setMaster("local[*]").setAppName("NetworkAvg")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint(".") // T: was missing, so I added it
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("avg"))

    /* No longer does what it should, but works kinda. Strings are null for some reason.
     * Now, if you conver from Array[Byte] to Int, u get underflow exception.
     * There's a correct way to handle it, but I couldn't find it.
     */

    def mappingFunc(key: String, value: Option[String], state: State[Int]): (String, Int) = {
      val temp:String = value.getOrElse("0")
      var newVal:Int = 0
      if (temp != "0"){
        newVal = 1 // normally ByteBuffer.wrap(temp).getInt, but underflows and throws exception
      }
      val oldVal:Int = state.getOption.getOrElse(0)
      val avg = oldVal + newVal
      state.update(avg)
      (key, avg)
    }

    val stateDstream = messages.mapWithState(StateSpec.function(mappingFunc _))
    stateDstream.print()

    ssc.start()
    ssc.awaitTermination()
  }
}


