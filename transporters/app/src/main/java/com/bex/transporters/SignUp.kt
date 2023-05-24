package com.bex.transporters

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import com.bex.transporters.models.User
import com.bex.transporters.pages.client.ClientPage
import com.bex.transporters.pages.driver.DriverPage
import com.bex.transporters.ui.theme.TransportersTheme
import com.squareup.moshi.Moshi
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

class SignUp : AppCompatActivity() {
    private val client = OkHttpClient()


    private lateinit var fname: String
    private lateinit var lname: String
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var phone: String
    private lateinit var type: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        val spinner: Spinner = findViewById(R.id.user_type_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_type,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

                val item = parent.getItemAtPosition(pos)
                type = parent.getItemAtPosition(pos).toString()
                Toast.makeText(this@SignUp, "Selected user type: $item", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        Thread {
            val login = findViewById<TextView>(R.id.logintext)
            login.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            val signupbutton = findViewById<Button>(R.id.signupbutton)
            signupbutton.setOnClickListener {
                fname = findViewById<EditText>(R.id.newuserfname).text.toString()
                lname = findViewById<EditText>(R.id.newuserlname).text.toString()
                username = findViewById<EditText>(R.id.newuserusername).text.toString()
                email = findViewById<EditText>(R.id.newuseremail).text.toString()
                password = findViewById<EditText>(R.id.newuserpassword).text.toString()
                phone = findViewById<EditText>(R.id.newuserphone).text.toString()
                newUser()

            }


        }.start()

    }

    private fun newUser() {
        val paramBuilder = FormBody.Builder()

        paramBuilder
            .add("user_first_name", fname)
            .add("user_last_name", lname)
            .add("user_username", username)
            .add("user_phone", "+212 $phone")
            .add("user_email", email)
            .add("user_password", password)
            .add("user_type", type)


        val formBody: RequestBody = paramBuilder.build()

        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/users/addUsers.php")
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
                        val toast = Toast.makeText(
                            applicationContext,
                            "Account Created! Use your credentials to log in",
                            Toast.LENGTH_LONG
                        )

                        toast.show()
                    }

                }
            }
        })
    }

}

