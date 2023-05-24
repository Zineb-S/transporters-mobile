package com.bex.transporters.pages.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Demand
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException


class ClientDemands : Fragment() {
    private var userId: Int = 0
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Demand::class.java)
    val moshiAdapter: JsonAdapter<List<Demand>> = moshi.adapter(listType)
    private lateinit var recyclerView: RecyclerView
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("UserTypePrefs", AppCompatActivity.MODE_PRIVATE)


        userId = sharedPreferences.getInt("userID", 0)
       // userId = arguments?.getInt("user_id", 0) ?: 0
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Thread {
            loadDemands()

        }.start()
    }

    private fun loadDemands() {

        val paramBuilder = FormBody.Builder()


        paramBuilder
            .add("demand_client_id", userId.toString())
        val formBody: RequestBody = paramBuilder.build()
        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/demands/getDemandsbyClientID.php")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val result = response.body!!.string()

            println(result)
            val grades = result.toString()
            var gr: List<Demand> = moshiAdapter.fromJson(grades) as List<Demand>
            val studentGrades = context?.let { DemandsAdapter(gr, userId, it) }
            activity?.runOnUiThread {
                recyclerView.adapter = studentGrades
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_demands, container, false)
        recyclerView = view.findViewById(R.id.DemandsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Set up the OnClickListener for the button
        val buttonNewDemand = view.findViewById<Button>(R.id.newclientdemandbtn)
        buttonNewDemand.setOnClickListener {
            val intent = Intent(activity, NewDemand::class.java)
            Log.i("printing","$userId")
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        return view
        // Inflate the layout for this fragment

    }

    companion object {
        @JvmStatic
        fun newInstance(userId: Int) =
            ClientDemands().apply {
                arguments = Bundle().apply {
                    putInt("user_id", userId)
                }
            }
    }
}