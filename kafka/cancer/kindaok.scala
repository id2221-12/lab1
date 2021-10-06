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

    val conf = new SparkConf().setMaster("local[*]").setAppName("NetworkAvg")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint(".") // T: was missing, so I added it
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("avg")).map {case (k,v) => v.split(",")}.map {case Array(k,v) => (k,v.toInt)}

    def mappingFunc(key: String, value: Option[Int], state: State[Int]): (String, Int) = {
      val newVal:Int = value.getOrElse(0)
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

