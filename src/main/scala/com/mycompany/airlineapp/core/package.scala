package com.mycompany.airlineapp.core

import org.tresamigos.smv._, panel._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Column
import java.util.{Calendar, Date}
import org.apache.spark.sql.expressions.Window

object Utils {
  def calDist(lat1: Column, lat2: Column, lon1: Column, lon2: Column) = {
    val la1 = lat1.cast(DoubleType)*3.14159/180
    val la2 = lat2.cast(DoubleType)*3.14159/180
    val a = la1 - la2
    val b = (lon1.cast(DoubleType) - lon2.cast(DoubleType))*3.14159/180
    val sa2 = sin(a / 2.0)
    val sb2 = sin(b / 2.0)
    lit(2)*lit(3960)*asin(sqrt(sa2*sa2 + cos(la1)*cos(la2)*sb2*sb2))
  }

  def monthdiff(d: String, n:Integer) = {
    lit(d).smvStrToTimestamp("yyyy-MM-dd").smvPlusMonths(n)
  }
}

object DateConf {
  val today = "2014-07-31"
  }

object FileConf {
  val csv_comma_noheader = new CsvAttributes(',' , '\"' , false)
}

object WindowConf {
  val win_cml5seg = Window.partitionBy("CUST_ID").orderBy("FLT_LEG_DPRT_TM").rowsBetween(-4,0)
  val win_grpcm = Window.partitionBy("CUST_ID")
  val win_allcm = Window.partitionBy()
  val win_grppnr = Window.partitionBy("CUST_ID", "PNR", "PNR_CREATION_DT")
}
