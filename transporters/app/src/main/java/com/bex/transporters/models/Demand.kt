package com.bex.transporters.models



data class Demand(val demand_id: Int,
                  val demand_cargo: String,
                  val demand_dimensions: String,
                  val demand_truck_type: String,
                  val demand_pickup_location: String,
                  val demand_destination_location: String,
                  val demand_date: String,
                  val demand_payment_type: String,
                  val demand_client_id: Int)
