import org.apache.spark.rdd._
import org.apache.spark.sql.DataFrame 




object SQL {
  
  def minMaxAvg(df: DataFrame) = {
    df.select(min("size"), max("size"), avg("size")).show()
  }

  def largestPop(df: DataFrame) = {
    val maxPop = df.select(max("hits"))
    df.select(max(""))
  }
  
}
