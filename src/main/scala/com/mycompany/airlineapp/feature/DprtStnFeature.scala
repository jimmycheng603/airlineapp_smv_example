package com.mycompany.airlineapp.feature

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.DateConf
import com.mycompany.airlineapp.core.WindowConf

object DprtStnFeature extends SmvModule("Features of departure stations") with SmvOutput {

  override def requiresDS() = Seq(input.FlightMaster)

  override def run(i: runParams) = {
    val df = i(input.FlightMaster)

    import df.sqlContext.implicits._
    val stn_punc = df.
    groupBy("SCH_LEG_ORIG_CD", "FLT_LEG_DPRT_DT").agg(
      sum(when($"DPRT_DELAY_MINUTES" > 0, 0.0).otherwise(1.0)) / count($"flight_leg_id") as "depstnday_flt_punc"
    ).
    groupBy("SCH_LEG_ORIG_CD").agg(
      avg(when($"FLT_LEG_DPRT_DT".smvStrToTimestamp("yyyy-MM-dd").smvPlusMonths(3) > lit(DateConf.today).smvStrToTimestamp("yyyy-MM-dd"), $"depstnday_flt_punc")) as "depstnavg_daily_punc"
    )

    df.smvSelectPlus(
      $"FLT_LEG_DPRT_DT".smvStrToTimestamp("yyyy-MM-dd") as "flt_leg_dprt_ts",
      lit(DateConf.today).smvStrToTimestamp("yyyy-MM-dd") as "today"
    ).
    groupBy("SCH_LEG_ORIG_CD").agg(
      avg(when(($"flt_leg_dprt_ts".smvPlusDays(7) > $"today") && $"haul_type" === "L", $"DPRT_DELAY_MINUTES")) as "depstnavg_lh_dly_mins_p1w",
      avg(when(($"flt_leg_dprt_ts".smvPlusDays(7) > $"today") && $"haul_type" === "S", $"DPRT_DELAY_MINUTES")) as "depstnavg_sh_dly_mins_p1w",
      sum(when(($"flt_leg_dprt_ts".smvPlusMonths(1) > $"today") && $"DPRT_DELAY_MINUTES" > 180, 1.0)) as "depstncnt_dly_3hr_p1m"
    ).
    smvJoinByKey(stn_punc, Seq("SCH_LEG_ORIG_CD"), SmvJoinType.LeftOuter)

  }
}
