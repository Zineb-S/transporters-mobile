package com.bex.transporters.models

data class Review(val review_id:Int,
                  val review_client_id:Int,
                  val review_driver_id:Int,
                  val review_content:String,
                  val user_username:String,
                  )
