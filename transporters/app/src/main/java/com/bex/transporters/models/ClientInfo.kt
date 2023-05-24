package com.bex.transporters.models

data class ClientInfo(
    val user_id: Int,
    val user_first_name: String,
    val user_last_name: String,
    val user_username: String,
    val user_email: String,
    val user_password: String,
    val user_phone: String,
    val user_type: String,
    val user_status: String,
    val demand_count: Int
)
