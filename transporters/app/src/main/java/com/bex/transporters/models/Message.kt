package com.bex.transporters.models

data class Message(val message_id :Int,
                   val offer_id:Int,
                   val sender_id:Int,
                   val receiver_id:Int,
                   val message_text:String,
                   val message_date:String)
