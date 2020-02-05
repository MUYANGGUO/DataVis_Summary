// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")

// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

// ENTER THE CODE BELOW
val DropSame=df.dropDuplicates().filter("answerer != questioner")
DropSame.show()

// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.

// ENTER THE CODE BELOW
val AnswererRank=DropSame.groupBy("answerer").count().withColumnRenamed("count", "questions_answered").sort($"questions_answered".desc, $"answerer".asc).limit(3)
AnswererRank.show()

// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.

// ENTER THE CODE BELOW
val QuestionerRank=DropSame.groupBy("questioner").count().withColumnRenamed("count", "questions_asked").sort($"questions_asked".desc, $"questioner".asc).limit(3)
QuestionerRank.show()

// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.

// ENTER THE CODE BELOW
val highestTotalDegree=DropSame.groupBy($"answerer", $"questioner").count().sort($"count".desc,$"answerer".asc,$"questioner".asc).limit(5)
highestTotalDegree.show()


// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.

// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp
// ENTER THE CODE BELOW
var Date = DropSame.withColumn("date", when(from_unixtime($"date").isNull, $"date").otherwise(from_unixtime($"date"))).filter(DropSame("date").gt(lit("2010-09-01"))).filter(DropSame("date").lt(lit("2011-01-01"))).withColumnRenamed("date","month")
var Month = Date.withColumn("month", month((Date("month")))).groupBy($"month").count().filter("month>=9").sort($"month".asc).withColumnRenamed("count","total_interactions")

Month.show()


// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.

// ENTER THE CODE BELOW
val QuestionerRank_Total=DropSame.groupBy("questioner").count().withColumnRenamed("count", "questions_asked").sort($"questions_asked".desc, $"questioner".asc)
val AnswererRank_Total=DropSame.groupBy("answerer").count().withColumnRenamed("count", "questions_answered").sort($"questions_answered".desc, $"answerer".asc)
val Rank_Total=QuestionerRank_Total.join(AnswererRank_Total, QuestionerRank_Total("questioner")=== AnswererRank_Total("answerer"), "full_outer").withColumn("questioner", when($"questioner".isNull, $"answerer").otherwise($"questioner")).withColumn("answerer", when($"answerer".isNull, $"questioner").otherwise($"answerer")).na.fill(0).select($"questioner", $"questions_asked", $"questions_answered").withColumn("total_activity",$"questions_asked"+$"questions_answered").select($"questioner",$"total_activity").withColumnRenamed("questioner", "userID").sort($"total_activity".desc,$"userID".asc).limit(3)

Rank_Total.show()

