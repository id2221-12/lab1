package sparkstreaming

import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder, Decoder}
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
      "zookeeper.connection.timeout.ms" -> "1000")
    val conf = new SparkConf().setMaster("local[*]").setAppName("NetworkAvg")
    val ssc = new StreamingContext(conf, Seconds(1))
    val messages = KafkaUtils.createDirectStream[String, Int, Decoder[String], Decoder[Int]](ssc, kafkaConf, Set("avg"))

    def mappingFunc(key: String, value: Option[Int], state: State[(Int, Double)]): (String, (Int, Double)) = {
      val newVal:Int = value.getOrElse(0)
      val oldVal:(Int, Double) = state.getOption.getOrElse((0, 0))
      val avg = (oldVal._1 + 1, (oldVal._2 * oldVal._1 + newVal) / (oldVal._1 + 1))
      state.update(avg)
      (key, avg)
    }

    val stateDstream = messages.mapWithState(StateSpec.function(mappingFunc _))

    ssc.start()
    ssc.awaitTermination()
  }
}

