package com.bex.transporters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.models.ClientInfo
import com.bex.transporters.models.Review
import com.bex.transporters.models.User
import com.bex.transporters.pages.ReviewAdapter
import com.bex.transporters.pages.client.ClientPage
import com.bex.transporters.pages.client.DemandsAdapter
import com.bex.transporters.pages.driver.DriverPage
import com.bex.transporters.ui.theme.TransportersTheme
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class VisitProfile : AppCompatActivity() {

    // Define UI components
    private lateinit var visitclientprofilefullname: TextView
    private lateinit var visitclientprofileusername: TextView
    private lateinit var visitclientprofilephone: TextView
    private lateinit var visitclientprofilenbdemands: TextView
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, ClientInfo::class.java)
    val moshiAdapter: JsonAdapter<List<ClientInfo>> = moshi.adapter(listType)

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var visitdriverprofilefullname: TextView
    private lateinit var visitdriverprofileusername: TextView
    private lateinit var visitdriverprofilephone: TextView
    private lateinit var visitdriverprofilenbdemands: TextView
    val listType2 = Types.newParameterizedType(List::class.java, DriverInfo::class.java)
    val moshiAdapter2: JsonAdapter<List<DriverInfo>> = moshi.adapter(listType2)
    val listType3 = Types.newParameterizedType(List::class.java, Review::class.java)
    val moshiAdapter3: JsonAdapter<List<Review>> = moshi.adapter(listType3)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var client_id = intent.getIntExtra("client_id", 0)
        var driver_id = intent.getIntExtra("driver_id", 0)
        if (client_id != 0) {
            setContentView(R.layout.visitclientprofile)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            visitclientprofilefullname = findViewById(R.id.visitclientprofilefullname)
            visitclientprofileusername = findViewById(R.id.visitclientprofileusername)
            visitclientprofilephone = findViewById(R.id.visitclientprofilephone)
            visitclientprofilenbdemands = findViewById(R.id.visitclientprofilenbdemands)

            val paramBuilder = FormBody.Builder()
            paramBuilder.add("client_id", client_id.toString())
            val formBody: RequestBody = paramBuilder.build()
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/users/clientInfo.php")
                .post(formBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }

                        val result = response.body!!.string()
                        val Jobject = JSONObject(result)
                        val Jarray = JSONArray()
                        Jarray.put(Jobject)
                        val users = Jarray.toString()
                        var userInfo: List<ClientInfo> =
                            moshiAdapter.fromJson(users) as List<ClientInfo>
                        runOnUiThread {
                            visitclientprofilefullname.text =
                                userInfo[0].user_first_name + " " + userInfo[0].user_last_name
                            visitclientprofileusername.text = userInfo[0].user_username
                            visitclientprofilephone.text = userInfo[0].user_phone
                            visitclientprofilenbdemands.text = userInfo[0].demand_count.toString()
                        }


                    }
                }
            })
        } else {
            if (driver_id != 0) {
                setContentView(R.layout.visitdriverprofile)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                val buttonreview = findViewById<Button>(R.id.visitdriverprofilesendreviewbtn)
                val reviewtext = findViewById<EditText>(R.id.visitdriverprofilereviewtext)
                buttonreview.setOnClickListener {
                    val sharedPreferences = getSharedPreferences(
                        "UserTypePrefs",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    val currentuserID = sharedPreferences.getInt("userID", 0)
                    val paramBuilder = FormBody.Builder()
                    paramBuilder.add("review_client_id", currentuserID.toString())
                    paramBuilder.add("review_driver_id", driver_id.toString())
                    paramBuilder.add("review_content", reviewtext.text.toString())

                    val formBody: RequestBody = paramBuilder.build()
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/transporters/reviews/addReview.php")
                        .post(formBody)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {

                            response.use {
                                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                for ((name, value) in response.headers) {
                                    println("$name: $value")
                                }
                                runOnUiThread {
                                    reviewtext.setText("")
                                    val toast = Toast.makeText(applicationContext, "Review Added", Toast.LENGTH_LONG)
                                    toast.show()
                                    }

                                fetchReviewsAndUpdateRecyclerView()
                            }
                        }
                    }
                    )
                }
            }
            runOnUiThread {
                visitdriverprofilefullname = findViewById(R.id.visitdriverprofilefullname)
                visitdriverprofileusername = findViewById(R.id.visitdriverprofileusername)
                visitdriverprofilephone = findViewById(R.id.visitdriverprofilephone)
                visitdriverprofilenbdemands = findViewById(R.id.visitdriverprofilenbtransports)
            }
            val paramBuilder = FormBody.Builder()
            paramBuilder.add("driver_id", driver_id.toString())
            val formBody: RequestBody = paramBuilder.build()
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/users/driverInfo.php")
                .post(formBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }

                        val result = response.body!!.string()
                        val Jobject = JSONObject(result)
                        val Jarray = JSONArray()
                        Jarray.put(Jobject)
                        val users = Jarray.toString()
                        var userInfo: List<DriverInfo> =
                            moshiAdapter2.fromJson(users) as List<DriverInfo>
                        runOnUiThread {
                        visitdriverprofilefullname.text =
                            userInfo[0].user_first_name + " " + userInfo[0].user_last_name
                        visitdriverprofileusername.text = "@" + userInfo[0].user_username
                        visitdriverprofilephone.text = userInfo[0].user_phone
                        visitdriverprofilenbdemands.text = userInfo[0].demand_count.toString()
                        }
                        val reviews = ReviewAdapter(userInfo[0].reviews)
                        runOnUiThread {

                            reviewsRecyclerView =
                                findViewById(R.id.visitdriverprofilereviewsrecyclerview)
                            reviewsRecyclerView.layoutManager =
                                LinearLayoutManager(this@VisitProfile)
                            reviewsRecyclerView.adapter = reviews
                        }

                    }
                }
            })
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val sharedPreferences =getSharedPreferences("UserTypePrefs",
                    AppCompatActivity.MODE_PRIVATE
                )
                var userType = sharedPreferences.getString("userType",null)
                if (userType == "driver") {
                    // start Driver home activity
                    val intent = Intent(this, DriverPage::class.java)
                    startActivity(intent)
                    finish()
                } else if (userType == "client") {
                    // start Client home activity
                    val intent = Intent(this, ClientPage::class.java)
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchReviewsAndUpdateRecyclerView() {
        var driver_id = intent.getIntExtra("driver_id", 0)
        val paramBuilder = FormBody.Builder()
        paramBuilder.add("driver_id", driver_id.toString())
        val formBody: RequestBody = paramBuilder.build()
        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/reviews/getReviewsbyDriverID.php")
            .post(formBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    val result = response.body!!.string()

                    val Jarray = JSONArray(result)

                    val users = Jarray.toString()
                    val reviewslist: List<Review> =
                        moshiAdapter3.fromJson(users) as List<Review>

                    runOnUiThread {
                        (reviewsRecyclerView.adapter as ReviewAdapter).updateData(reviewslist)
                    }
                }
            }
        })
    }



}




