package com.bex.transporters.pages.client

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bex.transporters.Constants
import com.bex.transporters.MainActivity

import com.bex.transporters.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class ClientProfile : Fragment() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_profile, container, false)
        val clientfname = view.findViewById<EditText>(R.id.profileFirstNameClient)
        val clientlname = view.findViewById<EditText>(R.id.profileLastNameClient)
        val clientemail = view.findViewById<EditText>(R.id.profileEmailClient)
        val clientusername = view.findViewById<EditText>(R.id.profileUsernameClient)
        val clientphone = view.findViewById<EditText>(R.id.profilePhoneClient)
        val update = view.findViewById<Button>(R.id.updateclientprofilebtn)
        val logout = view.findViewById<Button>(R.id.logoutbtnclient)
        logout.setOnClickListener { // Clear SharedPreferences
            val sharedPreferences = activity?.getSharedPreferences("UserTypePrefs",
                AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.clear()
            editor?.apply()

            // Navigate to LoginActivity
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        val sharedPreferences = activity?.getSharedPreferences("UserTypePrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        //editor.putInt("userID", userInfo[0].user_id)
        val userID = sharedPreferences?.getInt("userID", 0)
        val userfname = sharedPreferences?.getString("userFname",null)
        val userlname = sharedPreferences?.getString("userLname",null)
        val userusername = sharedPreferences?.getString("userUsername",null)
        val useremail = sharedPreferences?.getString("userEmail",null)
        val userpassword = sharedPreferences?.getString("userPassword",null)
        val userphone = sharedPreferences?.getString("userPhone",null)
        val userType = sharedPreferences?.getString("userType",null)
        val userstatus = sharedPreferences?.getString("userStatus",null)
        clientfname.setText(userfname)
        clientlname.setText(userlname)
        clientusername.setText( userusername)
        clientemail.setText(useremail)
        clientphone.setText(userphone)
        update.setOnClickListener {
            val paramBuilder = FormBody.Builder()

            paramBuilder
                .add("user_id", userID.toString())
                .add("user_first_name", clientfname.text.toString())
                .add("user_last_name", clientlname.text.toString())
                .add("user_username", clientusername.text.toString())
                .add("user_phone", clientphone.text.toString())
                .add("user_email", clientemail.text.toString())
                .add("user_password", userpassword.toString())
                .add("user_type", userType.toString())
                .add("user_status", userstatus.toString())


            val formBody: RequestBody = paramBuilder.build()

            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/users/updateUsers.php")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        // Create SharedPreferences editor
                        val editor = sharedPreferences?.edit()

                        // Update the SharedPreferences data
                        editor?.putString("userFname", clientfname.text.toString())
                        editor?.putString("userLname", clientlname.text.toString())
                        editor?.putString("userUsername", clientusername.text.toString())
                        editor?.putString("userEmail", clientemail.text.toString())
                        editor?.putString("userPhone", clientphone.text.toString())
                        editor?.putString("userPassword", userpassword)
                        editor?.putString("userType", userType)
                        editor?.putString("userStatus", userstatus)

                        // Commit the changes
                        editor?.apply()

                        activity?.runOnUiThread {
                            Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: Int) =
            ClientProfile().apply {
                arguments = Bundle().apply {
                    putInt("user_id", userId)
                }
            }
    }
}