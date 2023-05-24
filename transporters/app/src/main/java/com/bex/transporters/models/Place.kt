package com.bex.transporters.models

import com.google.android.gms.maps.model.LatLng

data class Place(
    var name: String,
    var address: String,
    var latLng: LatLng,
    var id: String,
    var message_sent: String,

)
