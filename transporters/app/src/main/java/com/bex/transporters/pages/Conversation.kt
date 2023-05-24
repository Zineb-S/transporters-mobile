package com.bex.transporters.pages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Demand
import com.bex.transporters.models.Message
import com.bex.transporters.pages.client.Map
import com.bex.transporters.pages.driver.DriverPage
import com.bex.transporters.services.DLocationListener
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Conversation : AppCompatActivity() {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Message::class.java)
    val moshiAdapter: JsonAdapter<List<Message>> = moshi.adapter(listType)
    val listType2 = Types.newParameterizedType(List::class.java, Demand::class.java)
    val moshiAdapter2: JsonAdapter<List<Demand>> = moshi.adapter(listType2)
    private lateinit var recyclerView: RecyclerView
    var newMsgOfferId = 0
    var newMsgSenderId = 0
    var newMsgReceiverId = 0
    private var msgsent: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.conversation)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Thread {
            loadMessages()
        }.start()

        val sendmsgbtn = findViewById<Button>(R.id.sendmsgbtn)
        val sendmsgedittext = findViewById<EditText>(R.id.sendmsgedittext)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val messageDate = dateFormat.format(Date())
        sendmsgbtn.setOnClickListener {
            val paramBuilder = FormBody.Builder()


            paramBuilder.add("offer_id", newMsgOfferId.toString())
                .add("sender_id", newMsgSenderId.toString())
                .add("receiver_id", newMsgReceiverId.toString())
                .add("message_text", sendmsgedittext.text.toString())
                .add("message_date", messageDate)


            val formBody: RequestBody = paramBuilder.build()
            val request =
                Request.Builder().url("${Constants.BASE_URL}/transporters/messages/addMessage.php")
                    .post(formBody).build()
            CoroutineScope(Dispatchers.IO).launch {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("New Message", "Failed to add message: ${response.body?.string()}")
                        throw IOException("Unexpected code $response")
                    } else {
                        Log.i("New Message", "Response body: ${response.body?.string()}")
                    }
                    withContext(Dispatchers.Main) {
                        sendmsgedittext.setText("")
                    }
                    loadMessages()
                    runOnUiThread {
                        Toast.makeText(this@Conversation, "Message Sent", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()  // Navigate back to the parent activity
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMessages() {

        val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)
        val userID = sharedPreferences.getInt("userID", 0)
        val userType = sharedPreferences.getString("userType", null)

        if (userType == "driver") {
            val trackingbtn = findViewById<Button>(R.id.drivertrackingbtn)
            trackingbtn.visibility = View.VISIBLE
            trackingbtn.setOnClickListener {
                checkLocationPermissions()
            }

            val paramBuilder = FormBody.Builder()
            val formBody: RequestBody = paramBuilder.build()
            val offer_id = intent.getIntExtra("offer_id", 0)
            val sender_id = intent.getIntExtra("sender_id", 0)
            val demand_id = intent.getIntExtra("demand_id", 0)

            // load notification
            val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putInt("demandID", demand_id)

            editor.apply()
            val request2 = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/demands/getDemandsbyDemandID.php")
                .post(FormBody.Builder().add("demand_id", demand_id.toString()).build()).build()
            client.newCall(request2).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, Demand::class.java)
                val adapter: JsonAdapter<List<Demand>> = moshi.adapter(listType)

                val demands = adapter.fromJson(response.body?.string())
                val clientId = demands?.firstOrNull()?.demand_client_id

                if (clientId != null) {
                    newMsgReceiverId = clientId
                    newMsgOfferId = offer_id
                    newMsgSenderId = sender_id
                    val receiver_id = clientId
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/transporters/messages/getConversation.php")
                        .post(
                            FormBody.Builder()
                                .add("offer_id", offer_id.toString()) // get from intent
                                .add("sender_id", sender_id.toString())// get from intent
                                .add("receiver_id", receiver_id.toString()).build()
                        ) // get demand client id from api
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val result = response.body!!.string()

                        println(result)
                        val grades = result.toString()
                        var gr: List<Message> = moshiAdapter.fromJson(grades) as List<Message>
                        println("Received ${gr.size} messages from the server")

                        val offers = MessageAdapter(gr, sender_id)
                        runOnUiThread {

                            recyclerView = findViewById(R.id.messagesrecyclerview)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = offers
                        }

                    }
                } else {
                    Log.i("error", "failed to get conversation")
                }
            }


        }

        else if (userType == "client") {
            val offer_id = intent.getIntExtra("offer_id", 0)
            val sender_id = intent.getIntExtra("sender_id", 0)
            val demand_id = intent.getIntExtra("demand_id", 0)
            val trackingbtn = findViewById<Button>(R.id.drivertrackingbtn)
            trackingbtn.visibility = View.VISIBLE
            trackingbtn.setOnClickListener {
                //checkLocationPermissions()
                val paramBuilder = FormBody.Builder()
                val formBody: RequestBody = paramBuilder.build()

                Thread(Runnable {
                    try {
                        val paramBuilder = FormBody.Builder().add("demand_id", demand_id.toString())
                        val formBody: RequestBody = paramBuilder.build()
                        val request = Request.Builder()
                            .url("http://192.168.100.80/transporters/demands/getDemandsbyDemandID.php")
                            .post(formBody).build()

                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val result = response.body!!.string()


                            val grades = result.toString()

                            var demand: List<Demand> = moshiAdapter2.fromJson(grades) as List<Demand>
                            println(" destination demand in conversation $demand")
                            val jsonObjectdestination = JSONObject(demand[0].demand_destination_location)

                            val msgJSONObject = jsonObjectdestination.getString("message_sent")

                            msgsent = msgJSONObject
                            //println(" destination demand in conversation $msgsent")
                            Log.d("MESSAGE SENT","${msgsent}")
                            if (Build.VERSION.SDK_INT >= 26) {
                                if (msgsent == "1") {
                                    Log.d("INSIDE CONDITION","wslna")
                                    val CHANNEL_ID = "driver_location"
                                    val channel = NotificationChannel(
                                        CHANNEL_ID,
                                        "Driver has arrived to the Destination",
                                        NotificationManager.IMPORTANCE_HIGH
                                    )
                                    val notificationManager =
                                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    notificationManager.createNotificationChannel(channel)
                                    val notification = NotificationCompat.Builder(this@Conversation, CHANNEL_ID)
                                        .setContentTitle("Driver has arrived to the Destination")
                                        .setContentText("Your driver is now at the destination!")
                                        .setSmallIcon(R.drawable.car).build()
                                    notificationManager.notify(1, notification)

                                    // update value to 2

                                            val paramBuilder = FormBody.Builder()
                                                .add("demand_id", demand_id.toString())
                                                .add("message_sent", "2")
                                            val formBody: RequestBody = paramBuilder.build()
                                            val request = Request.Builder()
                                                .url("http://192.168.100.80/transporters/demands/updateMessageSent.php")
                                                .post(formBody)
                                                .build()

                                            client.newCall(request).execute().use { response ->
                                                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                                val result = response.body!!.string()

                                                println("message updated $result")
                                                msgsent = "2"

                                            }


                                }
                            }

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

                val intent = Intent(this@Conversation, Map::class.java)
                startActivity(intent)
            }


            val clientId = intent.getIntExtra("driver_id", 0)

            if (clientId != null) {
                newMsgReceiverId = clientId
                newMsgOfferId = offer_id
                newMsgSenderId = sender_id
                val receiver_id = clientId
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/transporters/messages/getConversation.php").post(
                        FormBody.Builder().add("offer_id", offer_id.toString()) // get from intent
                            .add("sender_id", sender_id.toString())// get from intent
                            .add("receiver_id", receiver_id.toString()).build()
                    ) // get demand client id from api
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val result = response.body!!.string()

                    println(result)
                    val grades = result.toString()
                    var gr: List<Message> = moshiAdapter.fromJson(grades) as List<Message>
                    println("Received ${gr.size} messages from the server")

                    val offers = MessageAdapter(gr, sender_id)
                    runOnUiThread {

                        recyclerView = findViewById(R.id.messagesrecyclerview)
                        recyclerView.layoutManager = LinearLayoutManager(this)
                        recyclerView.adapter = offers
                    }

                }
            } else {
                Log.i("error", "failed to get conversation")
            }
        }


    }

    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermissions() {
        if (hasPermissions()) {

            val serviceIntent = Intent(applicationContext, DLocationListener()::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }


        } else {
        }
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        ActivityCompat.requestPermissions(this, permissions, 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)


                val userID = sharedPreferences.getInt("userID", 0)
                checkLocationPermissions()
            }
        }
    }
}


