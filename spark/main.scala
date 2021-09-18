import org.apache.spark.rdd._


case class Log(code: String, title: String, hits: Long, size: Long)

object Main extends Serializable {

  def make_log(line: String): Log = {
    val parts = line.split(' ')
    Log(parts(0), parts(1), parts(2).toLong, parts(3).toLong)
  }

  def make_log_rdd(string_rdd: RDD[String]): RDD[Log] = string_rdd.map(x => Main.make_log(x))

  def take_15(logs: RDD[Log]): Unit = logs.take(15).map(println) // 1

  def count(logs: RDD[Log]) = println(logs.count()) // 2

  def min_max_avg(logs: RDD[Log]) = { // 3
    val sizes = logs.map(x => x.size)
    val max = sizes.max
    val min = sizes.min
    val avg: Double = sizes.sum / sizes.count
    (min, max, avg)
  }

  def largest(logs: RDD[Log]) = {
    val max = logs.map(x => x.size).max
    logs.filter(x => x.size == max)
  }

  def largest_size(logs: RDD[Log]) = Main.largest(logs).collect() foreach println // 4
  
  def largest_pop(logs: RDD[Log]) = {val max = logs.map(x => x.hits).max; Main.largest(logs).filter(x => x.hits == max) foreach println} // 5

  def largest_title(logs: RDD[Log]) = { // 6
    val max = logs.map(x => x.title.length).max
    logs.filter(x => x.title.length == max) foreach println
  }
  
  def size_gt_avg(logs: RDD[Log]) = { // 7
    val (_, _, avg) = Main.min_max_avg(logs)
    logs.filter(x => x.size > avg)
  }

  def pageviews_by_project(logs: RDD[Log]) = { // 8
    logs.keyBy(x => x.code).groupByKey().mapValues(ls => ls.map(x => x.hits).sum) foreach println
  }

  def top_10_pageviews_by_project(logs: RDD[Log]) = { // 9
    logs.keyBy(x => x.code).groupByKey().mapValues(ls => ls.map(x => x.hits).sum).sortBy((_,hits) => hits).take(10) foreach println
  }

  def count_nonEnglish_the(logs: RDD[Log]) = logs.filter(l => l.title.startsWith("The") && !l.code.startsWith("en")).count // 10    

  def percentage_one_view(logs: RDD[Log]) = logs.filter(l => l.hits == 1).count * 100 / logs.count // 11

  def count_distinct_terms(logs: RDD[Log]) = logs.flatMap(l => l.title.toLowerCase.split("_")).distinct.count // 12 

  def most_freq_term(logs: RDD[Log]) = logs.flatMap(l => l.title.toLowerCase.split("_")).map(t => (t, 1)).reduceByKey(_+_).sortBy({case (_, v) => v}).collect.last // 13
}

def get_pgc() = sc.textFile("pagecounts-20160101-000000_parsed.out")
def get_logs() = Main.make_log_rdd(get_pgc())
