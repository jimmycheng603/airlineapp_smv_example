package com.mycompany.airlineapp.datamodel

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.tresamigos.smv._

//Derive customer master
object CustomerMaster extends SmvModule("Customer master with demo and FFP info") with SmvOutput {

  override def requiresDS() = Seq(input.DimCustomer, input.DimMember)

  override def run(i: runParams) = {
    val cm_df = i(input.DimCustomer)
    val mbr_df = i(input.DimMember)

    import cm_df.sqlContext.implicits._
    cm_df.smvJoinByKey(mbr_df, Seq("CUST_ID"), SmvJoinType.LeftOuter)
  }
}
