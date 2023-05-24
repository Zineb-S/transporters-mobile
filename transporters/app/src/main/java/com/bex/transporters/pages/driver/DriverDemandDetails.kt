package com.bex.transporters.pages.driver

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Demand
import com.bex.transporters.pages.client.ClientPage
import com.bex.transporters.pages.client.NewDemand

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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriverDemandDetails : AppCompatActivity() {
    private val client = OkHttpClient()
    private val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.driver_demands_details)

        val cargotypeTextView = findViewById<TextView>(R.id.cargotypeTextViewDetails)
        val dimensionsTextView = findViewById<TextView>(R.id.dimensionsTextViewDetails)
        val trucktypeTextView = findViewById<TextView>(R.id.trucktypeTextViewDetails)
        val indexTextView = findViewById<TextView>(R.id.indexTextViewDetails)
        val paymenttypeTextView = findViewById<TextView>(R.id.paymenttypeTextViewDetails)
        val dateTextView = findViewById<TextView>(R.id.dateTextViewDetails)
        val pickuplocationTextView = findViewById<TextView>(R.id.pickuplocationTextViewDetails)
        val destinationTextView = findViewById<TextView>(R.id.destinationTextViewDetails)
        val newoffermessage = findViewById<EditText>(R.id.newdrivermsgnewoffer)
        val buttonNewOffer = findViewById<Button>(R.id.newofferdriverbtn)
        val driverId = intent.getIntExtra("driver_id", 0)
        Log.i("driver id in details is ", "$driverId")
        val demandId = intent.getIntExtra("demand_id", 0)
        cargotypeTextView.text = intent.getStringExtra("demand_cargo")
        destinationTextView.text = intent.getStringExtra("demand_destination_location")
        trucktypeTextView.text = intent.getStringExtra("demand_truck_type")
        indexTextView.text = demandId.toString()
        paymenttypeTextView.text = intent.getStringExtra("demand_payment_type")
        dateTextView.text = intent.getStringExtra("demand_date")
        dimensionsTextView.text = intent.getStringExtra("demand_dimensions")
        pickuplocationTextView.text = intent.getStringExtra("demand_pickup_location")

        buttonNewOffer.setOnClickListener {
            val paramBuilder = FormBody.Builder()
            paramBuilder.add("driver_id", driverId.toString())
                .add("demand_id", demandId.toString())
            val formBody: RequestBody = paramBuilder.build()
            val request1 = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/offers/addOffers.php")
                .post(formBody)
                .build()
            val request2 = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/demands/getDemandsbyDemandID.php")
                .post(FormBody.Builder().add("demand_id", demandId.toString()).build())
                .build()


            CoroutineScope(Dispatchers.IO).launch {
                client.newCall(request2).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val moshi = Moshi.Builder().build()
                    val listType = Types.newParameterizedType(List::class.java, Demand::class.java)
                    val adapter: JsonAdapter<List<Demand>> = moshi.adapter(listType)

                    val demands = adapter.fromJson(response.body?.string())
                    val clientId = demands?.firstOrNull()?.demand_client_id

                    if (clientId != null) {
                        // use clientId here
                        try {

                            client.newCall(request1).execute().use { response ->
                                if (!response.isSuccessful) {
                                    Log.e(
                                        "NewOffer",
                                        "Failed to add offer: ${response.body?.string()}"
                                    )
                                    throw IOException("Unexpected code $response")
                                } else {
                                    val responseBody = response.body?.string()
                                    Log.i("NewOffer", "Response body: $responseBody")
                                    val offerId = responseBody?.toIntOrNull()
                                    Log.i("NewOffer", "New offer ID: $offerId")
                                    val paramBuilder2 = FormBody.Builder()
                                    val dateFormat =
                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val messageDate = dateFormat.format(Date())
                                    paramBuilder2.add("sender_id", driverId.toString())
                                        .add("offer_id", offerId.toString())
                                        .add(
                                            "receiver_id",
                                            clientId.toString()
                                        ) // replace with actual receiver_id
                                        .add("message_text", newoffermessage.text.toString())
                                        .add(
                                            "message_date",
                                            messageDate
                                        ) // replace with actual message_date

                                    val formBody2: RequestBody = paramBuilder2.build()
                                    val request3 = Request.Builder()
                                        .url("${Constants.BASE_URL}/transporters/messages/addMessage.php")
                                        .post(formBody2)
                                        .build()

                                    client.newCall(request3).execute().use { response2 ->
                                        if (!response2.isSuccessful) {
                                            Log.e(
                                                "NewMessage",
                                                "Failed to add message: ${response2.body?.string()}"
                                            )
                                            throw IOException("Unexpected code $response2")
                                        } else {
                                            Log.i(
                                                "NewMessage",
                                                "Response body: ${response2.body?.string()}"
                                            )
                                        }
                                    }


                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@DriverDemandDetails,
                                        "New Offer Added",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val fragment = DriverDemands().apply {
                                        arguments = Bundle().apply {
                                            putInt("driverId", driverId)
                                        }
                                    }

// Use the FragmentManager to replace the current view with the new fragment
                                    supportFragmentManager
                                        .beginTransaction()
                                        // Replace the contents of the container with the new fragment
                                        // In this example, R.id.fragment_container is the ID of a ViewGroup in your layout where you want to place the Fragment
                                        .replace(R.id.demandsdriver, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                        } catch (e: IOException) {
                            // Handle exception
                        }
                    } else {
                        Log.i("Error", "Failed to add message")
                    }

                }
            }
        }

    }
}
