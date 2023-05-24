package com.bex.transporters.pages.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Demand
import com.bex.transporters.pages.client.DemandsAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class DriverDemands : Fragment() {
    private var driverId: Int = 0
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Demand::class.java)
    val moshiAdapter: JsonAdapter<List<Demand>> = moshi.adapter(listType)
    private lateinit var recyclerView: RecyclerView
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = activity?.getSharedPreferences("UserTypePrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        if (sharedPreferences != null) {
            driverId = sharedPreferences.getInt("userID",0)
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private fun loadDemands() {

        val paramBuilder = FormBody.Builder()


        paramBuilder.add("demand_driver_id", driverId.toString())
        val formBody: RequestBody = paramBuilder.build()
        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/demands/getDemands.php")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val result = response.body!!.string()

            println(result)
            val grades = result.toString()
            var gr: List<Demand> = moshiAdapter.fromJson(grades) as List<Demand>
            val studentGrades = context?.let { DemandsAdapter(gr,driverId, it) }
            activity?.runOnUiThread {
                recyclerView.adapter = studentGrades
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_driver_demands, container, false)
        recyclerView = view.findViewById(R.id.driverdemandsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            loadDemands()

        }.start()
    }
    companion object {
        @JvmStatic
        fun newInstance(driverId: Int) =
            DriverDemands().apply {
                arguments = Bundle().apply {
                    putInt("driver_id", driverId)
                }
            }
    }
}