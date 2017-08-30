package com.mycompany.airlineapp.app

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.clustering.KMeans

/** Note to users:
This is a dummy module where users can further explore ml or mllib packages supported by the respective spark version to do the model development here
*/

object KMeansCluster extends SmvModule("Customer KMeans Clustering Module") {

  override def requiresDS() = Seq(input.CustomerAllFeatureTreat)

  override def run(i: runParams) = {
    val df = i(input.CustomerAllFeatureTreat)

    //Select features
    val assembler = new VectorAssembler().setInputCols(Array("cmcnt_seg_tvl_p1y", "cmcnt_lh_seg_tvl_p1y", "cmsum_rev_tvl_p1y", "cmint_age_now")).setOutputCol("features")
    val df_feature = assembler.transform(df)

    //Fit model
    val kmeans = new KMeans().setK(3)
    val model = kmeans.fit(df_feature)

    //Prediction
    model.transform(df_feature).drop("features")
  }
}

object KMeansClusterWithPCA extends SmvModule("Customer KMeans Clustering Module") {

  override def requiresDS() = Seq(input.CustomerAllFeatureTreat)

  override def run(i: runParams) = {
    val df = i(input.CustomerAllFeatureTreat)
    import df.sqlContext.implicits._

    //Select features
    val assembler = new VectorAssembler().setInputCols(Array("cmcnt_seg_tvl_p1y", "cmcnt_lh_seg_tvl_p1y", "cmsum_rev_tvl_p1y", "cmint_age_now")).setOutputCol("features")
    val df_feature = assembler.transform(df)

    //PCA
    val pca = new PCA().setK(2).setInputCol("features").setOutputCol("pca_features")
    val pca_model = pca.fit(df_feature)
    val df_pca = pca_model.transform(df_feature).drop("df_feature").smvSelectPlus($"pca_features" as "features")

    //Fit kmeans model
    val kmeans = new KMeans().setK(3)
    val km_model = kmeans.fit(df_pca)

    //Prediction
    km_model.transform(df_pca).drop("features")
  }
}
