package com.mycompany.airlineapp.feature

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.WindowConf

object CustomerAllFeatureJoin extends SmvModule("Combine all features of each customer") with SmvOutput {

  override def requiresDS() = Seq(CustomerProfileFeature, CustomerTravelFeature)

  override def run(i: runParams) = {
    val feat_df1 = i(CustomerProfileFeature)
    val feat_df2 = i(CustomerTravelFeature)

    import feat_df1.sqlContext.implicits._
    feat_df1.smvJoinByKey(feat_df2, Seq("CUST_ID"), SmvJoinType.LeftOuter)
  }
}


object CustomerAllFeatureTreat extends SmvModule("Treatments of features after join") with SmvOutput {

  override def requiresDS() = Seq(CustomerAllFeatureJoin)

  override def run(i: runParams) = {
    val df = i(CustomerAllFeatureJoin)

    import df.sqlContext.implicits._
    val feat_df = df.
    smvSelectPlus(
      avg($"cmint_age_now").over(WindowConf.win_allcm) as "cmint_avg_age_now"
    )

    feat_df.smvSelectPlus(
      when($"cmstr_gender_cd" === "F", 1).otherwise(0) as "cmind_gender_f",
      when($"cmstr_gender_cd" === "M", 1).otherwise(0) as "cmind_gender_m",
      when($"cmstr_tier_now" === "0", 1).otherwise(0) as "cmind_tier0_now",
      when($"cmstr_tier_now" === "1", 1).otherwise(0) as "cmind_tier1_now",
      when($"cmstr_tier_now" === "2", 1).otherwise(0) as "cmind_tier2_now",
      when($"cmstr_tier_now" === "3", 1).otherwise(0) as "cmind_tier3_now",
      coalesce($"cmint_age_now", $"cmint_avg_age_now") as "cmint_age_now_2",
      coalesce($"cmcnt_seg_tvl_p1y", lit(0.0)) as "cmcnt_seg_tvl_p1y_2",
      coalesce($"cmcnt_lh_seg_tvl_p1y", lit(0.0)) as "cmcnt_lh_seg_tvl_p1y_2",
      coalesce($"cmsum_rev_tvl_p1y", lit(0.0)) as "cmsum_rev_tvl_p1y_2"
    ).
    smvSelectMinus(
      $"cmint_age_now",
      $"cmcnt_seg_tvl_p1y",
      $"cmcnt_lh_seg_tvl_p1y",
      $"cmsum_rev_tvl_p1y"
    ).
    smvRenameField(
      "cmcnt_seg_tvl_p1y_2" -> "cmcnt_seg_tvl_p1y",
      "cmcnt_lh_seg_tvl_p1y_2" -> "cmcnt_lh_seg_tvl_p1y",
      "cmsum_rev_tvl_p1y_2" -> "cmsum_rev_tvl_p1y",
      "cmint_age_now_2" -> "cmint_age_now"
    )
  }
}
