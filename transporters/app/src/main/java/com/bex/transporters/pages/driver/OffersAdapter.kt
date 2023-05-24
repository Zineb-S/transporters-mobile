package com.bex.transporters.pages.driver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.bex.transporters.VisitProfile
import com.bex.transporters.models.Offer
import com.bex.transporters.pages.Conversation
import com.bex.transporters.pages.client.ClientProfile
import kotlinx.coroutines.NonDisposableHandle.parent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException


class OffersAdapter(private var gra: MutableList<Offer> , val sender_id : Int) :
    RecyclerView.Adapter<OffersAdapter.ViewHolder>() {
    private val senderId = sender_id
    private lateinit var acceptbtn : Button
    private lateinit var declinebtn : Button
    private val client = OkHttpClient()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.driver_offer_card, parent, false)
        val sharedPreferences = parent.context.getSharedPreferences("UserTypePrefs",
            AppCompatActivity.MODE_PRIVATE
        )

        val userType = sharedPreferences?.getString("userType",null)
        if (userType == "client"){
             acceptbtn = view.findViewById<Button>(R.id.acceptofferbtn)
            acceptbtn.visibility = View.VISIBLE

            declinebtn = view.findViewById<Button>(R.id.declineofferbtn)
            declinebtn.visibility = View.VISIBLE
        }else{
             acceptbtn = view.findViewById<Button>(R.id.acceptofferbtn)
            acceptbtn.visibility = View.GONE
            declinebtn = view.findViewById<Button>(R.id.declineofferbtn)
            declinebtn.visibility = View.GONE
        }


        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // get the data

        val offer = gra[position]
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Conversation::class.java)
            intent.putExtra("offer_id", offer.offer_id)
            intent.putExtra("demand_id", offer.demand_id)
            intent.putExtra("driver_id", offer.driver_id)
            intent.putExtra("sender_id", senderId)

            context.startActivity(intent)
        }
        // assign the data to the corresponding UI element
        holder.offerCardDriverOfferID.text = offer.offer_id.toString()
        holder.offerCardDriverDemandID.text = offer.demand_id.toString()
        holder.offerCardDriverDriverID.text = offer.driver_id.toString()
        val context = holder.itemView.context
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val userID = sharedPreferences.getInt("userID", 0)
        Log.d("OffersAdapter", "UserID: $userID, OfferDriverID: ${offer.driver_id}")
        if (userID == offer.driver_id){
            Log.d("OffersAdapter", "Hiding button for driver with ID: ${offer.driver_id}")
            holder.driverdetailsbtn.visibility =View.GONE
        } else {
            Log.d("OffersAdapter", "Showing button for driver with ID: ${offer.driver_id}")
            holder.driverdetailsbtn.visibility =View.VISIBLE
            holder.driverdetailsbtn.setOnClickListener {
                val intent = Intent(context, VisitProfile::class.java)
                intent.putExtra("driver_id",offer.driver_id)
                context.startActivity(intent)
            }
        }

        acceptbtn.setOnClickListener {

            val paramBuilder = FormBody.Builder()

            paramBuilder
                .add("demand_id", offer.demand_id.toString())
                .add("demand_driver_id", offer.driver_id.toString())



            val formBody: RequestBody = paramBuilder.build()

            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/demands/setDriverID.php")
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

                        val context = holder.itemView.context

                        (context as AppCompatActivity).runOnUiThread {
                            val toast = Toast.makeText(
                                context,
                                "Offer Accepted",
                                Toast.LENGTH_LONG
                            )

                            toast.show()
                        }

                    }
                }
            })
            acceptbtn.visibility = View.GONE
            declinebtn.visibility =View.GONE
        }
        declinebtn.setOnClickListener {
            val paramBuilder = FormBody.Builder()
            val adapterPosition = holder.adapterPosition

            paramBuilder.add("offer_id", gra[adapterPosition].offer_id.toString())
            val formBody: RequestBody = paramBuilder.build()
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/offers/deleteOffer.php")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val context = holder.itemView.context
                        (context as AppCompatActivity).runOnUiThread {
                            val toast = Toast.makeText(
                                context,
                                "Offer Deleted",
                                Toast.LENGTH_LONG
                            )
                            toast.show()

                            gra.removeAt(adapterPosition)
                            notifyDataSetChanged()
                        }
                    }
                }
            })
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return gra.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val offerCardDriverOfferID = itemView.findViewById<TextView>(R.id.offerCardDriverOfferID)
        val offerCardDriverDemandID = itemView.findViewById<TextView>(R.id.offerCardDriverDemandID)
        val offerCardDriverDriverID = itemView.findViewById<TextView>(R.id.offerCardDriverDriverID)
        val driverdetailsbtn = itemView.findViewById<Button>(R.id.driverdetailsbtn)

    }
}
