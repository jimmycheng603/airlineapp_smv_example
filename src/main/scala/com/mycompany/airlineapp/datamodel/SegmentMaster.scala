package com.mycompany.airlineapp.datamodel

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.DateConf
import com.mycompany.airlineapp.core.Utils._

//Derive segment master

object SegmentMaster extends SmvModule("Derive flight segment master") with SmvOutput {

  override def requiresDS() = Seq(input.FactBooking, input.FactTicket, input.FactCheckIn)

  override def run(i: runParams) = {
    val bkg_df = i(input.FactBooking)
    val tkt_df = i(input.FactTicket)
    val ckin_df = i(input.FactCheckIn)

    import bkg_df.sqlContext.implicits._
    val bkg_df_dedup = bkg_df.dedupByKeyWithOrder($"psj_id")($"BK_INACTIVE_TM".desc)

    bkg_df_dedup.
    smvJoinByKey(tkt_df, Seq("CUST_ID", "PNR", "PNR_CREATION_DT", "FLT_LEG_DPRT_DT", "SCH_LEG_ORIG_CD", "SCH_LEG_DEST_CD"), SmvJoinType.LeftOuter).
    smvJoinByKey(ckin_df, Seq("CUST_ID", "PNR", "PNR_CREATION_DT", "SCH_LEG_ORIG_CD", "FLT_LEG_DPRT_DT", "FLT_NUM"), SmvJoinType.LeftOuter).
    smvSelectPlus(
      $"FLT_LEG_DPRT_DT".smvStrToTimestamp("yyyy-MM-dd") as "flt_leg_dprt_ts",
      ($"CKIN_SEQ_NUM".isNotNull || $"CKIN_TM".isNotNull || $"SEAT".isNotNull) as "travel_flag"
    ).
    smvSelectPlus(
      $"flt_leg_dprt_ts" > monthdiff(DateConf.today,-12) as "ind_flt_leg_dprt_p1y"
    )

  }
}
