import org.apache.spark.rdd._


case class Log(code: String, title: String, hits: Long, size: Long)

object Main extends Serializable {

  def makeLog(line: String): Log = {
    val parts = line.split(' ')
    Log(parts(0), parts(1), parts(2).toLong, parts(3).toLong)
  }

  def makeLogRDD(stringRDD: RDD[String]): RDD[Log] = stringRDD.map(x => Main.makeLog(x))

  def take15(logs: RDD[Log]): Unit = logs.take(15).map(println) // 1

  def count(logs: RDD[Log]) = println(logs.count()) // 2

  def minMaxAvg(logs: RDD[Log]) = { // 3
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

  def largestSize(logs: RDD[Log]) = Main.largest(logs).collect() foreach println // 4
  
  def largestPop(logs: RDD[Log]) = {val max = logs.map(x => x.hits).max; Main.largest(logs).filter(x => x.hits == max) foreach println} // 5

  def largestTitle(logs: RDD[Log]) = { // 6
    val max = logs.map(x => x.title.length).max
    logs.filter(x => x.title.length == max) foreach println
  }
  
  def sizeGTAvg(logs: RDD[Log]) = { // 7
    val (_, _, avg) = Main.minMaxAvg(logs)
    logs.filter(x => x.size > avg) foreach println
  }

  def pageviewsByProject(logs: RDD[Log]) = { // 8
    logs.keyBy(x => x.code).groupByKey().mapValues(ls => ls.map(x => x.hits).sum) foreach println
  }

  def top10PageviewsByProject(logs: RDD[Log]) = { // 9
    logs.keyBy(x => x.code).groupByKey().mapValues(ls => ls.map(x => x.hits).sum).sortBy({case (_,hits) => hits}).take(10) foreach println
  }

  def countNonEnglishThe(logs: RDD[Log]) = logs.filter(l => l.title.startsWith("The") && !l.code.startsWith("en")).count // 10    

  def percentageOneView(logs: RDD[Log]) = logs.filter(l => l.hits == 1).count * 100 / logs.count // 11

  def countDistinctTerms(logs: RDD[Log]) = logs.flatMap(l => l.title.toLowerCase.split("_")).distinct.count // 12 

  def mostFreqTerm(logs: RDD[Log]) = logs.flatMap(l => l.title.toLowerCase.split("_")).map(t => (t, 1)).reduceByKey(_+_).sortBy({case (_, v) => v}).collect.last // 13
}

def getPgc() = sc.textFile("pagecounts-20160101-000000_parsed.out")
def getLogs() = Main.makeLogRDD(getPgc())
