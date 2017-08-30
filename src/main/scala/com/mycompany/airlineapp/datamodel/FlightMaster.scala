package com.mycompany.airlineapp.datamodel

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.Utils._

//Derive flight level master
object FlightMaster extends SmvModule("Drive flight level master") with SmvOutput {

  override def requiresDS() = Seq(input.DimFlightLeg, input.DimGeo)

  override def run(i: runParams) = {
    val flt_df = i(input.DimFlightLeg)
    val geo_df = i(input.DimGeo)

    import flt_df.sqlContext.implicits._
    flt_df.join(geo_df, $"SCH_LEG_ORIG_CD" === $"LOCATION_ID", "left_outer").
    drop("LOCATION_ID").
    smvSelectPlus(
      $"SCH_LEG_DEST_CD" as "LOCATION_ID"
    ).
    smvJoinByKey(geo_df, Seq("LOCATION_ID"), SmvJoinType.LeftOuter, "_DEST").
    smvSelectPlus(
      calDist($"LATITUDE", $"LATITUDE_DEST", $"LONGITUDE", $"LONGITUDE_DEST") as "orig_dest_dist"
    ).
    smvSelectPlus(
      when($"orig_dest_dist" > 3000, "L").otherwise("S") as "haul_type",
      substring($"FLT_LEG_DPRT_TM", 12, 2) as "flt_leg_dprt_hr"
    )
  }
}
