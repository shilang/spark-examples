package com.yhd.spark.ml


import com.yhd.spark.common.SparkEntry
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StandardScaler, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, GBTRegressor}
import org.apache.spark.sql.expressions._
import org.apache.spark.sql.functions.{lit, _}

/**
 * VM options: -Xms4G -Xmx4G -Xmn768m -XX:PermSize=384m -XX:MaxPermSize=384m
 */
object RegressorExample extends SparkEntry{

  def main(args: Array[String]) {

    val recordsRawData = spark.table("pms.label_feat_rel_sim_cate_73_78")
    val testRawData = spark.table("pms.label_feat_rel_sim_cate_79")

    val records = recordsRawData.withColumn("type", lit("train"))
    val testRecords = testRawData.withColumn("type", lit("test"))
    val finalSampleData = records.union(testRecords).cache()

    // similar to zipWithIndex
    val productId = finalSampleData.select("product_id").distinct().withColumn("product_id_row_num",
      row_number.over(Window.partitionBy(lit(1)).orderBy(lit(1))))
    val userId = finalSampleData.select("user_id").distinct().withColumn("user_id_row_num",
      row_number.over(Window.partitionBy(lit(1)).orderBy(lit(1))))

    val numProductId = productId.count()
    val numUserId = userId.count()

    logInfo("numProductId:" + numProductId)
    logInfo("numUserId:" + numUserId)

    spark.udf.register("position", (n: Int, P: Int) => {
      val position = Array.ofDim[Double](P)
      position(n) = 1.0
      position
    })

    spark.udf.register("log", (d: Double) => {
      math.log(d + 0.001)
    })

    val columns = records.columns
    val newCol = Array("label",
      "position(product_id_row_num, " + numProductId + ") as product_id_row_num",
      "position(user_id_row_num, " + numUserId + ") user_id_row_num",
      "type")
    val selectCols = newCol ++ columns.slice(1, 69).map(x => "log(" + x + ") as " + x)

    val joinedData = finalSampleData.
      join(productId, finalSampleData("product_id") === productId("product_id"), "inner").
      join(userId, finalSampleData("user_id") === userId("user_id"), "inner").cache()

    // val dataCategories =  joinedData.select(columns.head, columns.tail:_*)
    val dataCategories = joinedData.selectExpr(selectCols : _*)

    val labelIndexer = new StringIndexer().
      setInputCol("label").
      setOutputCol("labeled")

    val vectorAssembler = new VectorAssembler().
      setInputCols(columns.slice(1, 69)).
      setOutputCol("indexedFeatures")

    val scaler = new StandardScaler()
      .setInputCol(vectorAssembler.getOutputCol)
      .setOutputCol("scaledFeatures")
      .setWithStd(true)
      .setWithMean(false)

    // Train a DecisionTree model.
    val dt = new GBTRegressor().
      setLabelCol(labelIndexer.getOutputCol).
      setFeaturesCol(scaler.getOutputCol)

    // Chain indexer and tree in a Pipeline.
    val pipeline = new Pipeline().
      setStages(Array(labelIndexer, vectorAssembler, scaler, dt))

    val trainingData = dataCategories.where("type = 'train'")
    val testData = dataCategories.where("type = 'test'")

    // Train model. This also runs the indexer.
    val model = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "label", "indexedFeatures").show(5)

    // Select (prediction, true label) and compute test error.
    val evaluator = new RegressionEvaluator().setLabelCol("label")
      .setPredictionCol("prediction").setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)

    // scalastyle:off
    logInfo("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val treeModel = model.stages(1).asInstanceOf[DecisionTreeRegressionModel]
    logInfo("Learned regression tree model:\n" + treeModel.toDebugString)

    spark.stop()
  }
}
