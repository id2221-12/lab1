import org.apache.spark.rdd._
import org.apache.spark.sql.DataFrame 


object SQL {
  
  def minMaxAvg(df: DataFrame) = {
    df.select(min("size").alias("min"), max("size").alias("max"), avg("size").alias("avg")).show
  }

  def largestPop(df: DataFrame) = {
    
    
    df.createOrReplaceTempView("df_view")

    spark.sql("""select * from df_view 
      where size = (select max(size) from df_view) 
      order by hits""").show
  }
  
  def sizeGTAvg(df: DataFrame) = {
    val mma = SQL.minMaxAvg(df)
    mma.createOrReplaceTempView("m_view")
    df.createOrReplaceTempView("df_view")

    spark.sql("""select * from df_view
      where size > (select avg from m_view) 
      """).show
  }
}
