package com.bex.transporters.pages.client

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bex.transporters.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import com.google.android.gms.maps.model.PolylineOptions
class Map : AppCompatActivity() {
    private var path: MutableList<LatLng> = ArrayList()
    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var driverMarker: Marker? = null
    internal var isDriverMarkerMoving = false
    internal var isMarkerRotating = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_fragment)
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            this.googleMap = googleMap
            listenLocationOfDriver()
        }


    }

    fun rotateMarker(marker: Marker, toRotation: Float) {
        if (!isMarkerRotating) {
            val handler = Handler(Looper.getMainLooper())
            val start = SystemClock.uptimeMillis()
            val startRotation = marker.rotation
            val duration: Long = 1000
            val interpolator = LinearInterpolator()
            handler.post(object : Runnable {
                override fun run() {
                    isMarkerRotating = true
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val rot = t * toRotation + (1 - t) * startRotation
                    marker.rotation = if (-rot > 180) rot / 2 else rot
                    if (t < 1.0) {
                        handler.postDelayed(this, 16)
                    } else {
                        isMarkerRotating = false
                    }
                }
            })
        }
    }

    fun animateMarker(
        googleMap: GoogleMap,
        driverMarker: Marker,
        toPosition: LatLng,
        hideMarker: Boolean
    ) {
        if (!isDriverMarkerMoving) {
            val start = SystemClock.uptimeMillis()
            val proj = googleMap.projection
            val startPoint = proj.toScreenLocation(driverMarker.position)
            val startLatLng = proj.fromScreenLocation(startPoint)
            val duration: Long = 2000

            val interpolator = LinearInterpolator()

            val driverMarkerHandler = Handler(Looper.getMainLooper())
            driverMarkerHandler.post(object : Runnable {
                override fun run() {
                    isDriverMarkerMoving = true
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
                    val lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude
                    driverMarker.position = LatLng(lat, lng)

                    if (t < 1.0) {
                        driverMarkerHandler.postDelayed(this, 16)
                    } else {
                        driverMarker.isVisible = !hideMarker
                        isDriverMarkerMoving = false
                    }
                }
            })
        }
    }

    private fun listenLocationOfDriver() {
        val database =
            Firebase.database("https://transporters-e5f96-default-rtdb.europe-west1.firebasedatabase.app/")
        val dbRef: DatabaseReference? = database.reference
//AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH DRIVER POINTSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        dbRef?.child("driver_points")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("onDataChange", "${snapshot}")
                val value = snapshot.value as? HashMap<*, *>
                val latitude = (value?.get("latitude") ?: 0.0).toString().toDouble()
                val longitude = (value?.get("longitude") ?: 0.0).toString().toDouble()
                val latLng = LatLng(latitude, longitude)
                googleMap?.let {
                    path.add(latLng)  // Add the new location to the path

                    // Create a PolylineOptions object
                    val polylineOptions = PolylineOptions()
                        .addAll(path)
                        .color(Color.RED)  // Change this to change the color of the polyline
                        .width(10f)        // Change this to change the width of the polyline

                    // Add the Polyline to the map
                    googleMap!!.addPolyline(polylineOptions)
                    if (driverMarker == null) {
                        driverMarker = it.addMarker(
                            MarkerOptions().position(latLng) //icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).anchor(0.5f, 0.5f).flat(true)

                        )
                    } else {
                        driverMarker?.position?.let { oldPosition ->
                            val bearing = bearingBetweenLocations(oldPosition, latLng)
                            rotateMarker(driverMarker!!, bearing.toFloat())
                            animateMarker(it, driverMarker!!, latLng, false)
                        }

                    }
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,30.0f))
                }

                /*   val sydney = LatLng(-33.852, 151.211)
                   googleMap!!.addMarker(
                       MarkerOptions()
                           .position(sydney)
                           .title("Marker in Sydney")
                   )
                   googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
        val lat1 = latLng1.latitude
        val lng1 = latLng1.longitude
        val lat2 = latLng2.latitude
        val lng2 = latLng2.longitude
        val fLat: Double = degreeToRadians(lat1)
        val fLong: Double = degreeToRadians(lng1)
        val tLat: Double = degreeToRadians(lat2)
        val tLong: Double = degreeToRadians(lng2)
        val dLon = tLong - fLong
        val degree: Double = radiansToDegree(
            atan2(
                sin(dLon) * cos(tLat),
                cos(fLat) * sin(tLat) - sin(fLat) * cos(tLat) * cos(dLon)
            )
        )
        return if (degree >= 0) {
            degree
        } else {
            360 + degree
        }
    }

    private fun degreeToRadians(latLong: Double): Double {
        return Math.PI * latLong / 180.0
    }

    private fun radiansToDegree(latLong: Double): Double {
        return latLong * 180.0 / Math.PI
    }

}

