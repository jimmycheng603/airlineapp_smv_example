
package com.mycompany.airlineapp.etl

import org.tresamigos.smv._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark.sql.DataFrame


object FactBooking extends SmvModule("Flight booking fact table") with SmvOutput {

  override def requiresDS() = Seq(input.BookingTable)
  override def run(i: runParams) = {
    val df = i(input.BookingTable)

    import df.sqlContext.implicits._
    df.select(
      $"PNR",
      $"PNR_CREATION_DT",
      $"CUST_ID",
      $"BK_ACTIVE_TM",
      $"BK_INACTIVE_TM",
      $"FLT_NUM",
      $"CARRIER_CD",
      $"FLT_LEG_DPRT_DT",
      $"FLT_LEG_DPRT_TM",
      $"FLT_LEG_ARRV_TM",
      $"SCH_LEG_ORIG_CD",
      $"SCH_LEG_DEST_CD",
      $"CABIN",
      $"CLASS",
      $"NUM_PAX",
      $"ACTION_CD",
      $"SEG_NUM",
      $"CONN_NEXT",
      $"CONN_PREV",
      concat($"CARRIER_CD", $"FLT_NUM", $"FLT_LEG_DPRT_DT", $"SCH_LEG_ORIG_CD", $"SCH_LEG_DEST_CD") as "flight_leg_id",
      concat($"CUST_ID", $"PNR", $"PNR_CREATION_DT", $"FLT_LEG_DPRT_DT", $"SCH_LEG_ORIG_CD", $"SCH_LEG_DEST_CD") as "psj_id"
    )
  }
}

object FactTicket extends SmvModule("Ticketing fact table") with SmvOutput {

  override def requiresDS() = Seq(input.TicketTable)
  override def run(i: runParams) = {
    val df = i(input.TicketTable)

    import df.sqlContext.implicits._
    df.select(
      $"TKT_NUM",
      $"ISSUED_DT",
      $"REV_UPDATE_DT",
      $"COUPON_NUM",
      $"POINT_OF_ISSUE_CD",
      $"POS_COUNTRY_CD",
      $"REV_TKT_IND",
      $"MKTG_FLT_NUM",
      $"SCH_LEG_ORIG_CD",
      $"SCH_LEG_DEST_CD",
      $"FLT_LEG_DPRT_DT",
      $"REV_AMT",
      $"FUEL_SURCHARGE_AMT",
      $"CUST_ID",
      $"PNR",
      $"PNR_CREATION_DT"
    )

  }
}

object FactCheckIn extends SmvModule("Passenger check-in fact table") with SmvOutput {

  override def requiresDS() = Seq(input.CheckInTable)
  override def run(i: runParams) = {
    val df = i(input.CheckInTable)

    import df.sqlContext.implicits._
    df.smvSelectMinus(
      $"CKIN_CARRIER_CD",
      $"CKIN_CLASS"
    )
  }
}
