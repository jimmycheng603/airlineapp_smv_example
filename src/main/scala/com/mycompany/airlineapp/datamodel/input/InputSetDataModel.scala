package com.mycompany.airlineapp.datamodel.input

import org.tresamigos.smv._
import com.mycompany.airlineapp.etl

//for customer master
object DimCustomer extends SmvModuleLink(etl.DimCustomer)
object DimMember extends SmvModuleLink(etl.DimMember)

//for segment master
object FactBooking extends SmvModuleLink(etl.FactBooking)
object FactTicket extends SmvModuleLink(etl.FactTicket)
object FactCheckIn extends SmvModuleLink(etl.FactCheckIn)

//for flight master
object DimFlightLeg extends SmvModuleLink(etl.DimFlightLeg)
object DimGeo extends SmvModuleLink(etl.DimGeo)
