import org.apache.spark.rdd._
import org.apache.spark.sql.DataFrame 
import org.apache.spark.sql.functions.typedLit

object SQL {
  
  def minMaxAvg(df: DataFrame) = {
    df.select(min("size").alias("min_size"),
      max("size").alias("max_size"),
      avg("size").alias("avg_size"))
  }

  def largestPop(df: DataFrame) = {
    df.createOrReplaceTempView("df_view")

    spark.sql("""
      select * from df_view 
      where size = (select max(size) from df_view) 
      order by hits
      """).show
  }
  
  def sizeGTAvg(df: DataFrame) = {
    val mma = SQL.minMaxAvg(df)
    mma.createOrReplaceTempView("m_view")
    df.createOrReplaceTempView("df_view")

    spark.sql("""
      select * from df_view
      where size > (select avg_size from m_view)
      """).show
  }

  def countDistinctTerms(df: DataFrame) = {
    df.flatMap(f => f.getString(1).toLowerCase.split("_")).distinct.count
  }

  def mostFreqTerm(df: DataFrame) = {
    df.flatMap(f => f.getString(1).toLowerCase.split("_"))
      .map(t => (t,1))
      .groupBy("_1")
      .sum("_2")
      .orderBy(desc("sum(_2)"))
      .first
  }
}
