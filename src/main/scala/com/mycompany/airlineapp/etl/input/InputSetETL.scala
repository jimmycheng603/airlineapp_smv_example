package com.mycompany.airlineapp.etl.input

import org.tresamigos.smv._, dqm._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import com.mycompany.airlineapp.core.FileConf
import com.mycompany.airlineapp.core.DateConf


object CustomerTable extends SmvCsvFile("customer.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
  override def dqm() = SmvDQM().
    add(FailParserCountPolicy(3)).
    add(DQMRule((col("BIRTH_MM") >= 1 && col("BIRTH_MM") <=12) || col("BIRTH_MM").isNull, "rule_birth_mm", FailAny)).
    add(DQMRule((col("BIRTH_DD") >= 1 && col("BIRTH_DD") <= 31) || col("BIRTH_DD").isNull, "rule_birth_dd", FailAny)).
    add(DQMFix((col("BIRTH_DD") < 1 || col("BIRTH_DD") > 31), lit(null) as "BIRTH_DD", "fix_birth_dd"))
}

object MemberTable extends SmvCsvFile("member_lvl.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
}

object BookingTable extends SmvCsvFile("booking.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
  override def dqm() = SmvDQM().
    add(DQMRule(col("PNR").isNotNull, "rule_pnr", FailAny)).
    add(DQMRule(col("PNR_CREATION_DT") <= DateConf.today, "rule_pnr_dt", FailPercent(0.001)))
}

object TicketTable extends SmvCsvFile("ticket.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
}

object CheckInTable extends SmvCsvFile("checkin.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
}

object GeoRef extends SmvCsvFile("ref_geo.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
}

object FlightLegTable extends SmvCsvFile("flight_leg_snapshot.csv"){
  val csvAttributes = FileConf.csv_comma_noheader
}
