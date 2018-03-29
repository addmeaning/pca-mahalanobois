import breeze.linalg.DenseVector
import breeze.linalg.inv
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Matrix
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType

object SparkApp extends App {
  val session = SparkSession.builder()
    .appName("spark-app").master("local[*]").getOrCreate()

  import session.implicits._

  val df = Seq(
    (1, 4, 0),
    (3, 4, 0),
    (1, 3, 0),
    (3, 3, 0),
    (67, 37, 0) //outlier
  ).toDF("x", "y", "z")
  val vectorAssembler = new VectorAssembler().setInputCols(Array("x", "y", "z")).setOutputCol("vector")
  val standardScalar = new StandardScaler().setInputCol("vector").setOutputCol("normalized-vector").setWithMean(true)
    .setWithStd(true)

  val pca = new PCA().setInputCol("normalized-vector").setOutputCol("pca-features").setK(2)

  val pipeline = new Pipeline().setStages(
    Array(vectorAssembler, standardScalar, pca)
  )

  val pcaDF = pipeline.fit(df).transform(df)

  def withMahalanobois(df: DataFrame, inputCol: String): DataFrame = {
    val Row(coeff1: Matrix) = Correlation.corr(df, inputCol).head

    val invCovariance = inv(new breeze.linalg.DenseMatrix(2, 2, coeff1.toArray))

    val mahalanobois = udf[Double, Vector] { v =>
      val vB = DenseVector(v.toArray)
      vB.t * invCovariance * vB
    }

    df.withColumn("mahalanobois", mahalanobois(df(inputCol)))
  }

  withMahalanobois(pcaDF, "pca-features").show

  session.close()
}
