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
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

case class User(name: String, age: Int)

object GraphX {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("task3")
    val sc = new SparkContext(conf)
    val people: RDD[(VertexId, User)] = sc.parallelize(Array(
      (1L, User("Alice", 28)),
      (2L, User("Bob", 27)),
      (3L, User("Charlie", 65)),
      (4L, User("David", 42)),
      (5L, User("Ed", 55)),
      (6L, User("Fran", 50)),
      (7L, User("Alex", 55))
    )) 
    val likes: RDD[Edge[Int]] = sc.parallelize(Array(
      Edge(2L, 1L, 7),
      Edge(3L, 2L, 4),
      Edge(3L, 6L, 3),
      Edge(4L, 1L, 1),
      Edge(2L, 4L, 2),
      Edge(5L, 2L, 2),
      Edge(5L, 3L, 8),
      Edge(5L, 6L, 3),
      Edge(7L, 5L, 3),
      Edge(7L, 6L, 4)
    ))
    val defaultUser = User("Peter Capaldi", 5000)
    val graph: Graph[User, Int] = Graph(people, likes, defaultUser)

    // users over 30
    val over30 = graph.vertices.filter { case (_, User(_, age)) => age > 30 }
    println("Users over 30:")
    over30.collect.foreach({case (_, User(name, _)) => println(name)})
    println()

    // who likes who (directedness)
    println("Who likes Who?:")
    val rels = graph.triplets.map(t => (t.srcAttr.name, t.dstAttr.name))
    rels.collect.foreach({ case (a,b) => println(s"$a likes $b") })
    println()

    // likes more than 5 times = lovers
    println("Lovers:")
    val rels2 = graph.triplets.filter(t => t.attr > 5).map(t => (t.srcAttr.name, t.dstAttr.name))
    rels2.collect.foreach({ case (a,b) => println(s"$a lOVeS $b") })
    println()

    // Node cardinality
    println("In degree:")
    val rels3 = graph.outerJoinVertices(graph.inDegrees)((id, u, deg) => (u, deg.getOrElse(0)))
      .vertices.map {case (id, (User(name, _), deg)) => (name, deg)}
    rels3.collect.foreach(println)
    println()

    // nodes where out degree = in degree
    println("In == Out:")
    val rels4 = graph.outerJoinVertices(graph.inDegrees)((id, u, deg) => (u, deg.getOrElse(0)))
      .outerJoinVertices(graph.outDegrees)({ case (id, (u, in), deg) => (u, in, deg.getOrElse(0)) })
      .vertices.filter {case (id, (User(name, _), in, out)) => in == out}
    rels4.collect.foreach { case (_, (User(name,_), _, _)) => println(name) }
    println()

    // oldest follower
    println("Oldest follower:")
    val rels5 = graph.aggregateMessages[User](
      t => {t.sendToDst(t.srcAttr)},
      { case (a@User(_, age1), b@User(_, age2)) => { if (age1 > age2) a else b } }
    )
    val rels6 = graph.outerJoinVertices(rels5)((id, u, oldest) => (u, oldest)).vertices
    rels6.collect.foreach { 
      case (_,(User(name,_), Some(User(othername,age)))) => println(s"$name's oldest follower: $othername, $age")
      case _ => 
    } 

    sc.stop()
  }
}

// who thought this could be a good idea

