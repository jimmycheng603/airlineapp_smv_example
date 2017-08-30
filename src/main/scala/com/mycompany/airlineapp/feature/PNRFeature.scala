package com.mycompany.airlineapp.feature

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.DateConf
import com.mycompany.airlineapp.core.WindowConf

object PNRFeature extends SmvModule("Features of PNR") with SmvOutput {

  override def requiresDS() = Seq(input.SegmentMaster)

  override def run(i: runParams) = {
    val df = i(input.SegmentMaster)

    import df.sqlContext.implicits._
    df.groupBy("PNR", "PNR_CREATION_DT").agg(
      sum(when($"travel_flag", 1.0).otherwise(0.0)) as "pnrcnt_seg_tvl",
      countDistinct("CUST_ID") as "pnrcnt_uniq_cm"
    )
  }
}
