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
import com.bex.transporters.models.Offer
import com.bex.transporters.pages.client.DemandsAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DriverOffers : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var driverId: Int = 0
    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Offer::class.java)
    val moshiAdapter: JsonAdapter<List<Offer>> = moshi.adapter(listType)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //driverId = arguments?.getInt("driver_id", 0) ?: 0
        val sharedPreferences = requireContext().getSharedPreferences("UserTypePrefs", AppCompatActivity.MODE_PRIVATE)


        driverId = sharedPreferences.getInt("userID", 0)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            loadOffers()

        }.start()
    }
    private fun loadOffers() {
        val paramBuilder = FormBody.Builder()


        //paramBuilder.add("demand_client_id", driverId.toString())
        val formBody: RequestBody = paramBuilder.build()
        paramBuilder.add("driver_id", driverId.toString())
        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/offers/getOffersbyDriverID.php")
            .post(FormBody.Builder().add("driver_id", driverId.toString()).build())
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val result = response.body!!.string()

            println(result)
            val grades = result.toString()
            var gr: MutableList<Offer> = moshiAdapter.fromJson(grades) as MutableList<Offer>
            val offers = OffersAdapter(gr,driverId)
            activity?.runOnUiThread {
                recyclerView.adapter = offers
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_driver_offers, container, false)
        recyclerView = view.findViewById(R.id.driverOffersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        GlobalScope.launch(Dispatchers.IO) {
            // Call your network operation here
            loadOffers() // If this is the method that triggers the network call
        }

        // Inflate the layout for this fragment
        return view
        // Inflate the layout for this fragment

    }

    companion object {

        @JvmStatic
        fun newInstance(driverId: Int) =
            DriverOffers().apply {
                arguments = Bundle().apply {
                    putInt("driver_id", driverId)
                }
            }
    }
}