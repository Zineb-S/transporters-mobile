package com.bex.transporters.services

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build

import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Demand
import com.bex.transporters.pages.client.DemandsAdapter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Objects

class DLocationListener() : Service(), LocationListener {
    private var location: Location? = null
    private var locationRequest: LocationRequest? = null
    private var dataLoaded = false
    private val database =
        Firebase.database("https://transporters-e5f96-default-rtdb.europe-west1.firebasedatabase.app/")
    private val dbRef: DatabaseReference? = database.reference
    private var demandID: Int? = null
    private var destinationLatLng: LatLng = LatLng(0.0, 0.0)
    private var msgsent: String = ""
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Demand::class.java)
    val moshiAdapter: JsonAdapter<List<Demand>> = moshi.adapter(listType)

    override fun onLocationChanged(p0: Location) {
        val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)


        val ID = sharedPreferences.getInt("userID", 0)
        Log.d(
            "onLocationChanged:",
            "${p0.latitude} , ${p0.longitude} ,${ID} , ${destinationLatLng}"
        )
        val myLocation = com.bex.transporters.models.Location(
            driverID = ID,
            latitude = p0.latitude,
            longitude = p0.longitude
        )
        dbRef?.child("driver_points")?.setValue(myLocation)
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)
        demandID = sharedPreferences.getInt("demandID", 0)
        Log.d("DEMAND ID CHECK", "msgsent set to: '$demandID'")
        // get demand final destination latitude and longitude and put them in values
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "smt_location"
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name) + " using your location",
                NotificationManager.IMPORTANCE_HIGH
            )
            (Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Your location is being used by transporters")
                    .setContentText("Your location is being used by transporters")
                    .setSmallIcon(R.drawable.car)
                    .build()
            startForeground(1, notification)
        }
        Thread(Runnable {
            try {
                val paramBuilder = FormBody.Builder()
                    .add("demand_id", demandID.toString())
                val formBody: RequestBody = paramBuilder.build()
                val request = Request.Builder()
                    .url("http://192.168.100.80/transporters/demands/getDemandsbyDemandID.php")
                    .post(formBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val result = response.body!!.string()

                    println("destination demand $result")
                    val grades = result.toString()
                    var demand: List<Demand> = moshiAdapter.fromJson(grades) as List<Demand>
                    val jsonObjectdestination = JSONObject(demand[0].demand_destination_location)
                    Log.d("JSON_OBJECT", jsonObjectdestination.toString())
                    val latLngJSONObject = jsonObjectdestination.getJSONObject("latLng")
                    val msgJSONObject = jsonObjectdestination.getString("message_sent")

                    Log.d("MSGSENT_VALUE_SET", "msgsent set to: '$msgsent'")

                    val lat = latLngJSONObject.getDouble("latitude")
                    val lng = latLngJSONObject.getDouble("longitude")
                    msgsent = msgJSONObject
                    destinationLatLng = LatLng(lat, lng)
                    Log.d("MESSAGE SENT VALUE:", " ${msgsent}")
                    dataLoaded = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("NETWORK_EXCEPTION", "Network operation failed: ${e.message}")
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("JSON_EXCEPTION", "Failed to parse JSON: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("GENERAL_EXCEPTION", "An error occurred: ${e.message}")
            }
        }).start()
        //destinationLatLng = new value from demand
        //FirebaseApp.initializeApp(this)



        startLocationUpdate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LOCATION_UPDATES", "Starting location updates")
        startLocationUpdate()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startLocationUpdate() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 2500

        val builder = LocationSettingsRequest.Builder()
        locationRequest?.let { locReq ->
            builder.addLocationRequest(locReq)
            val locationSettingRequest = builder.build()
            val locationSetting = LocationServices.getSettingsClient(this)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getFusedLocationProviderClient(this).requestLocationUpdates(
                    locReq,
                    object : LocationCallback() {
                        override fun onLocationResult(p0: LocationResult) {
                            p0?.lastLocation?.let { lastLocation ->
                                if (dataLoaded) {
                                    onLocationChanged(lastLocation)
                                    val driverLocation = lastLocation

                                    destinationLatLng?.let { destination ->
                                        val finalDestinationLocation = Location("")
                                        finalDestinationLocation.latitude = destination.latitude
                                        finalDestinationLocation.longitude = destination.longitude

                                        val distanceToDestination =
                                            driverLocation.distanceTo(finalDestinationLocation)
                                        Log.d("DISTANCE_UPDATE", "Distance to destination: $distanceToDestination")

                                        if (distanceToDestination < 200) {
                                            Log.d("CONDITION_CHECK", "Distance is less than 200")
                                            if (Build.VERSION.SDK_INT >= 26) {
                                                if (msgsent == "0") {
                                                    val CHANNEL_ID = "driver_location"
                                                    val channel = NotificationChannel(
                                                        CHANNEL_ID,
                                                        "Driver has arrived to the Destination",
                                                        NotificationManager.IMPORTANCE_HIGH
                                                    )
                                                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                                                        channel
                                                    )
                                                    val notification =
                                                        NotificationCompat.Builder(
                                                            this@DLocationListener,
                                                            CHANNEL_ID
                                                        )
                                                            .setContentTitle("Driver has arrived to the Destination")
                                                            .setContentText("Your driver is now at the destination!")
                                                            .setSmallIcon(R.drawable.car)
                                                            .build()
                                                    startForeground(1, notification)

                                                    // update value to 1
                                                    Thread {
                                                        try {
                                                            val paramBuilder = FormBody.Builder()
                                                                .add("demand_id", demandID.toString())
                                                                .add("message_sent", "1")
                                                            val formBody: RequestBody = paramBuilder.build()
                                                            val request = Request.Builder()
                                                                .url("http://192.168.100.80/transporters/demands/updateMessageSent.php")
                                                                .post(formBody)
                                                                .build()
                                                            Log.d("NETWORK_REQUEST", "Sending request to update msgsent for demandID $demandID to $msgsent")
                                                            client.newCall(request).execute().use { response ->
                                                                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                                                val result = response.body!!.string()
                                                                println("message updated $result")
                                                                msgsent = "1"

                                                                Log.d("MESSAGE_UPDATE", "Message updated successfully: $msgsent")
                                                            }
                                                        } catch (e: IOException) {
                                                            e.printStackTrace()
                                                            Log.d("NETWORK_EXCEPTION", "Network operation failed: ${e.message}")
                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                            Log.d("JSON_EXCEPTION", "Failed to parse JSON: ${e.message}")
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                            Log.d("GENERAL_EXCEPTION", "An error occurred: ${e.message}")
                                                        }
                                                    }.start()


                                                }

                                            }
                                        }
                                    }
                                }

                            }
                        }
                    },
                    Looper.getMainLooper()
                )
            } else {
                if (Build.VERSION.SDK_INT >= 26) {
                    val CHANNEL_ID = "no permissions given"
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        getString(R.string.app_name) + " is not allowed to use your location",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    (Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager).createNotificationChannel(
                        channel
                    )
                    val notification =
                        NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("No permissions given")
                            .setContentText("No permissions given to transporters")
                            .setSmallIcon(R.drawable.car)
                            .build()
                    startForeground(3, notification)
                } else {

                }

            }
        }
    }


}