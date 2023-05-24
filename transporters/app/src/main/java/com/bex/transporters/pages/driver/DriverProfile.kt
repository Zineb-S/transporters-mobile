package com.bex.transporters.pages.driver

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var driverStatus: String

class DriverProfile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_driver_profile, container, false)

        val driverfname = view.findViewById<EditText>(R.id.profileFirstNameDriver)
        val driverlname = view.findViewById<EditText>(R.id.profileLastNameDriver)
        val driveremail = view.findViewById<EditText>(R.id.profileEmailDriver)
        val driverusername = view.findViewById<EditText>(R.id.profileUsernameDriver)
        val driverphone = view.findViewById<EditText>(R.id.profilePhoneDriver)
        val update = view.findViewById<Button>(R.id.updateDriverprofilebtn)
        val logout = view.findViewById<Button>(R.id.logoutbtndriver)
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
        val spinner: Spinner = view.findViewById(R.id.driverstatusspinner)
        val adapter = activity?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.driver_status,
                android.R.layout.simple_spinner_item
            )
        }
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val driverStatusArray = resources.getStringArray(R.array.driver_status)
        val driverStatusIndex = driverStatusArray.indexOf(userstatus)
        spinner.setSelection(driverStatusIndex)



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

                val item = parent.getItemAtPosition(pos)
                driverStatus = parent.getItemAtPosition(pos).toString()
                Toast.makeText(activity, "Selected user type: $item", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        driverfname.setText(userfname)
        driverlname.setText(userlname)
        driverusername.setText( userusername)
        driveremail.setText(useremail)
        driverphone.setText(userphone)
        update.setOnClickListener {
            val paramBuilder = FormBody.Builder()

            paramBuilder
                .add("user_id", userID.toString())
                .add("user_first_name", driverfname.text.toString())
                .add("user_last_name", driverlname.text.toString())
                .add("user_username", driverusername.text.toString())
                .add("user_phone", driverphone.text.toString())
                .add("user_email", driveremail.text.toString())
                .add("user_password", userpassword.toString())
                .add("user_type", userType.toString())
                .add("user_status", driverStatus)


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
                        editor?.putString("userFname", driverfname.text.toString())
                        editor?.putString("userLname", driverlname.text.toString())
                        editor?.putString("userUsername", driverusername.text.toString())
                        editor?.putString("userEmail", driveremail.text.toString())
                        editor?.putString("userPhone", driverphone.text.toString())
                        editor?.putString("userPassword", userpassword)
                        editor?.putString("userType", userType)
                        editor?.putString("userStatus", driverStatus)

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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DriverProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DriverProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}