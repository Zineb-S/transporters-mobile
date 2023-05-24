package com.bex.transporters.models

import com.google.firebase.database.IgnoreExtraProperties

//add driver ID
@IgnoreExtraProperties
data class Location(
    var driverID: Int, var latitude: Double? = null, var longitude: Double? = null
)