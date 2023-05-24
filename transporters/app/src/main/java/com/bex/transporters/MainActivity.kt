package com.bex.transporters

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bex.transporters.models.User
import com.bex.transporters.pages.client.ClientPage
import com.bex.transporters.pages.driver.DriverPage
import com.bex.transporters.ui.theme.TransportersTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
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

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    // moshi
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, User::class.java)
    val moshiAdapter: JsonAdapter<List<User>> = moshi.adapter(listType)

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon)

        FirebaseApp.initializeApp(this)
        //val database = Firebase.database("https://transporters-e5f96-default-rtdb.europe-west1.firebasedatabase.app/")
       /* val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                Log.d("TAG", "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })*/

        Thread {
            val signup = findViewById<TextView>(R.id.signuptext)
            signup.setOnClickListener {
                val intent =Intent(this,SignUp::class.java)
                startActivity(intent)
            }
            val loginButton = findViewById<Button>(R.id.loginButton)
            loginButton.setOnClickListener {
                validData()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }


        }.start()
    }

    private fun validData() {

        val email = findViewById<EditText>(R.id.emailText)
        val password = findViewById<EditText>(R.id.passwordText)


        val paramBuilder = FormBody.Builder()


        paramBuilder
            .add("user_email", email.text.toString())
            .add("user_password",password.text.toString())



        val formBody: RequestBody = paramBuilder.build()

        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/validateData.php")
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
                    var userInfo: List<User> = moshiAdapter.fromJson(users) as List<User>
                    if (userInfo[0].user_type!!.equals("driver"))
                    {
                        val intent = Intent(this@MainActivity, DriverPage::class.java)
                        intent.putExtra("driver_id", userInfo[0].user_id)
                        val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        editor.putInt("userID", userInfo[0].user_id)
                        editor.putString("userFname", userInfo[0].user_first_name)
                        editor.putString("userLname", userInfo[0].user_last_name)
                        editor.putString("userUsername", userInfo[0].user_username)
                        editor.putString("userEmail", userInfo[0].user_email)
                        editor.putString("userPassword", userInfo[0].user_password)
                        editor.putString("userPhone", userInfo[0].user_phone)
                        editor.putString("userType", userInfo[0].user_type)
                        editor.putString("userStatus", userInfo[0].user_status)
                        editor.apply()
                        runOnUiThread{
                            val toast = Toast.makeText(applicationContext, "Welcome Driver "+userInfo[0].user_username, Toast.LENGTH_LONG)
                            toast.show()
                        }
                        startActivity(intent)
                        finish()

                    }
                    if (userInfo[0].user_type!!.equals("client"))
                    {
                        val intent = Intent(this@MainActivity, ClientPage::class.java)
                        intent.putExtra("user_id", userInfo[0].user_id)
                        val sharedPreferences = getSharedPreferences("UserTypePrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("userID", userInfo[0].user_id)
                        editor.putString("userFname", userInfo[0].user_first_name)
                        editor.putString("userLname", userInfo[0].user_last_name)
                        editor.putString("userUsername", userInfo[0].user_username)
                        editor.putString("userEmail", userInfo[0].user_email)
                        editor.putString("userPassword", userInfo[0].user_password)
                        editor.putString("userPhone", userInfo[0].user_phone)
                        editor.putString("userType", userInfo[0].user_type)
                        editor.putString("userStatus", userInfo[0].user_status)
                        editor.apply()
                        runOnUiThread{
                            val toast = Toast.makeText(applicationContext, "Welcome Client "+userInfo[0].user_username, Toast.LENGTH_LONG)

                            toast.show()
                        }
                        startActivity(intent)
                        finish()

                    }



                }
            }
        })
    }
}
