package com.bex.transporters.pages.client

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.R
import com.bex.transporters.VisitProfile
import com.bex.transporters.models.Demand
import com.bex.transporters.models.Offer
import com.bex.transporters.models.Place
import com.bex.transporters.pages.driver.DriverDemandDetails
import org.json.JSONObject

class DemandsAdapter(private val gra: List<Demand>, driverId: Int ,private val context: Context): RecyclerView.Adapter<DemandsAdapter.ViewHolder>() {

    val driverId = driverId
    val sharedPreferences = context.getSharedPreferences("UserTypePrefs",
        AppCompatActivity.MODE_PRIVATE
    )
    val userID = sharedPreferences.getInt("userID", 0)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_demand_card, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println(gra)
        // get the data
        val demands = gra[position]


        val userType = sharedPreferences.getString("userType", null)


        val jsonObjectpickup = JSONObject(demands.demand_pickup_location)
        val jsonObjectdestination = JSONObject(demands.demand_destination_location)
        if (userType == "driver") {
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, DriverDemandDetails::class.java)
                intent.putExtra("demand_id", demands.demand_id)
                intent.putExtra("demand_cargo", demands.demand_cargo)
                intent.putExtra("demand_dimensions", demands.demand_dimensions)
                intent.putExtra("demand_truck_type", demands.demand_truck_type)
                intent.putExtra("demand_payment_type", demands.demand_payment_type)
                intent.putExtra("demand_date", demands.demand_date)
                intent.putExtra("demand_pickup_location", jsonObjectpickup.getString("name"))//demands.demand_pickup_location
                intent.putExtra("demand_destination_location",jsonObjectdestination.getString("name") )//demands.demand_destination_location
                intent.putExtra("driver_id", driverId)
                Log.i("driver id in adapter is ", "$driverId")
                context.startActivity(intent)
            }
        }

        holder.pickuplocationTextView.text = jsonObjectpickup.getString("name")

        holder.destinationTextView.text = jsonObjectdestination.getString("name")
        // assign the data to the corresponding UI element
        holder.indexTextView.text = demands.demand_id.toString()
        holder.cargotypeTextView.text = demands.demand_cargo
        holder.dimensionsTextView.text = demands.demand_dimensions
        holder.trucktypeTextView.text = demands.demand_truck_type +" " +"Truck"
        holder.paymenttypeTextView.text = demands.demand_payment_type
        holder.dateTextView.text = demands.demand_date
        //holder.pickuplocationTextView.text = demands.demand_pickup_location
       //holder.destinationTextView.text = demands.demand_destination_location


        if (userID == demands.demand_client_id){
            holder.clientdetailsbtn.visibility = View.GONE
        }
        holder.clientdetailsbtn.setOnClickListener {

            if (userID != demands.demand_client_id){
                val intent = Intent(context, VisitProfile::class.java)
                intent.putExtra("client_id",demands.demand_client_id)
                context.startActivity(intent)
            }
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return gra.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cargotypeTextView = itemView.findViewById<TextView>(R.id.cargotypeTextView)
        val dimensionsTextView = itemView.findViewById<TextView>(R.id.dimensionsTextView)
        val trucktypeTextView = itemView.findViewById<TextView>(R.id.trucktypeTextView)
        val indexTextView = itemView.findViewById<TextView>(R.id.indexTextView)
        val paymenttypeTextView = itemView.findViewById<TextView>(R.id.paymenttypeTextView)
        val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
        val pickuplocationTextView = itemView.findViewById<TextView>(R.id.pickuplocationTextView)
        val destinationTextView = itemView.findViewById<TextView>(R.id.destinationTextView)
        val clientdetailsbtn = itemView.findViewById<Button>(R.id.clientdetailsbtn)

    }
}
