package com.mycompany.airlineapp.feature

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

import com.mycompany.airlineapp.core.DateConf
import com.mycompany.airlineapp.core.WindowConf

object CustomerProfileFeature extends SmvModule("Features of customer demo and member profile") with SmvOutput {

  override def requiresDS() = Seq(input.CustomerMaster)

  override def run(i: runParams) = {
    val df = i(input.CustomerMaster)

    import df.sqlContext.implicits._
    df.select(
      $"CUST_ID",
      $"CURR_LVL" as "cmstr_tier_now",
      lit(DateConf.today).smvStrToTimestamp("yyyy-MM-dd").smvDay70 - $"ENROLL_DT".smvStrToTimestamp("yyyy-MM-dd").smvDay70 as "cmcnt_days_enroll_now",
      substring(lit(DateConf.today),1,4).cast(LongType) - $"BIRTH_YYYY" as "cmint_age_now",
      $"gender" as "cmstr_gender_cd"
    )

  }
}
