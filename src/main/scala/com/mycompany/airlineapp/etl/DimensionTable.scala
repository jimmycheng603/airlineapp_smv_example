
package com.mycompany.airlineapp.etl

import org.tresamigos.smv._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark.sql.DataFrame
import com.mycompany.airlineapp.core.Utils._

/**
 * ETL Modules of Dimension Tables
 */

object DimCustomer extends SmvModule("Customer dimension table") with SmvOutput {

  override def requiresDS() = Seq(input.CustomerTable)
  override def run(i: runParams) = {
    val df = i(input.CustomerTable)

    import df.sqlContext.implicits._
    df.select(
      $"CUST_ID",
      $"BIRTH_YYYY",
      $"BIRTH_MM",
      $"BIRTH_DD",
      coalesce($"GENDER_CD", lit("NA")) as "gender"
    )
  }
}

object DimMember extends SmvModule("FFP member dimension table") with SmvOutput {

  override def requiresDS() = Seq(input.MemberTable)
  override def run(i: runParams) = {
    i(input.MemberTable)
  }
}


object DimGeo extends SmvModule("Geographic dimension table") with SmvOutput {

  override def requiresDS() = Seq(input.GeoRef)
  override def run(i: runParams) = {
    i(input.GeoRef)
  }
}


object DimFlightLeg extends SmvModule("Flight leg dimension table") with SmvOutput {

  override def requiresDS() = Seq(input.FlightLegTable)
  override def run(i: runParams) = {
    val df = i(input.FlightLegTable)

    import df.sqlContext.implicits._
    df.smvSelectPlus(
      concat($"CARRIER_CD", $"FLT_NUM", $"FLT_LEG_DPRT_DT", $"SCH_LEG_ORIG_CD", $"SCH_LEG_DEST_CD") as "flight_leg_id"
    )
  }
}
