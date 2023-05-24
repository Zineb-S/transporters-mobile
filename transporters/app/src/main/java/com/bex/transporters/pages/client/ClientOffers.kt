package com.bex.transporters.pages.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.models.Offer
import com.bex.transporters.pages.driver.OffersAdapter
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


class ClientOffers : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private var userId: Int = 0
    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Offer::class.java)
    val moshiAdapter: JsonAdapter<List<Offer>> = moshi.adapter(listType)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        userId = arguments?.getInt("user_id", 0) ?: 0
    }

    private fun loadOffers() {
        val paramBuilder = FormBody.Builder()


        //paramBuilder.add("demand_client_id", driverId.toString())
        val formBody: RequestBody = paramBuilder.build()


        println("userID result: $userId")
        val request = Request.Builder()
            .url("${Constants.BASE_URL}/transporters/offers/getOffersbyClientID.php?client_id=${userId}")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val result = response.body!!.string()

            println(result)
            println("loadOffers result: $result")
            val grades = result.toString()
            var gr: MutableList<Offer> = moshiAdapter.fromJson(grades) as MutableList<Offer>

            println("OffersAdapter data: $gr")
            val offers = OffersAdapter(gr,userId)
            activity?.runOnUiThread {
                println("Setting the adapter.")
                recyclerView.adapter = offers
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            loadOffers()

        }.start()



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_client_offers, container, false)

        recyclerView = view.findViewById(R.id.clientoffersrecyclerview)
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
        fun newInstance(userId: Int) =
            ClientOffers().apply {
                arguments = Bundle().apply {
                    putInt("user_id", userId)
                }
            }
    }
}