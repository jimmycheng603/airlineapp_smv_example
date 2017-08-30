package com.mycompany.airlineapp.feature

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.Utils._
import com.mycompany.airlineapp.core.DateConf
import com.mycompany.airlineapp.core.WindowConf

object CustomerTravelFeature extends SmvModule("Features of customer's travel behaviors") with SmvOutput {

  override def requiresDS() = Seq(input.SegmentMaster, input.FlightMaster)

  override def run(i: runParams) = {
    val seg_df = i(input.SegmentMaster)
    val flt_df = i(input.FlightMaster)

    import seg_df.sqlContext.implicits._
    val df = seg_df.smvJoinByKey(flt_df, Seq("flight_leg_id"), SmvJoinType.LeftOuter)
    val df_prep = df.filter($"travel_flag").smvSelectPlus(
      sum($"DPRT_DELAY_IND").over(WindowConf.win_cml5seg) as "segcnt_seg_depdly_l5s",
      sum(lit(1)).over(WindowConf.win_grppnr) as "pnrcnt_seg_tvl",
      min($"flt_leg_dprt_ts").over(WindowConf.win_grppnr) as "pnrdat_1st_seg_tvl"
    )

    val df_cm = df_prep.orderBy(
      "CUST_ID",
      "FLT_LEG_DPRT_TM").
    groupBy("CUST_ID").agg(
      sum(when($"ind_flt_leg_dprt_p1y", 1.0).otherwise(0.0)) as "cmcnt_seg_tvl_p1y",
      sum(when($"ind_flt_leg_dprt_p1y", $"REV_AMT").otherwise(0.0)) as "cmsum_rev_tvl_p1y",
      sum(when(($"ind_flt_leg_dprt_p1y" && $"haul_type" === "L"), 1.0).otherwise(0.0)) as "cmcnt_lh_seg_tvl_p1y",
      sum(when($"ind_flt_leg_dprt_p1y", $"DPRT_DELAY_IND").otherwise(0.0)) as "cmcnt_seg_depdly_p1y",
      last($"segcnt_seg_depdly_l5s") as "cmcnt_seg_depdly_l5s",
      max($"flt_leg_dprt_ts") as "cmdat_seg_tvl_last"
    ).
    smvSelectPlus(
      $"cmcnt_lh_seg_tvl_p1y" / $"cmcnt_seg_tvl_p1y" as "cmpct_lh_seg_tvl_p1y",
      lit(DateConf.today).smvStrToTimestamp("yyyy-MM-dd").smvDay70 - $"cmdat_seg_tvl_last".smvDay70 as "cmcnt_days_last_flt"
    )

    val df_pnrcm = df_prep.
    orderBy("CUST_ID", "PNR", "PNR_CREATION_DT", "FLT_LEG_DPRT_TM").
    groupBy("CUST_ID", "PNR", "PNR_CREATION_DT").agg(
      last(when($"pnrcnt_seg_tvl">1, $"SCH_LEG_ORIG_CD").otherwise($"SCH_LEG_DEST_CD")) as "pnrstr_dst_cd",
      first($"pnrdat_1st_seg_tvl") as "pnrdat_1st_flt_dt"
    ).
    smvSelectPlus(
      $"pnrdat_1st_flt_dt".smvDay70 - $"PNR_CREATION_DT".smvStrToTimestamp("yyyy-MM-dd").smvDay70 as "pnrday_lead_bkg_flt"
    ).groupBy("CUST_ID").agg(
      mfvStr($"pnrstr_dst_cd") as "cmstr_mf_dst_cd",
      avg($"pnrday_lead_bkg_flt") as "cmavg_lead_bkg_flt"
    )

    df_cm.smvJoinByKey(df_pnrcm, Seq("CUST_ID"), SmvJoinType.LeftOuter)
  }
}
