package com.bex.transporters.pages.client

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bex.transporters.Constants
import com.bex.transporters.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.Calendar
import com.google.gson.Gson


class NewDemand : AppCompatActivity() {
    private lateinit var pickupLocationPlace : com.bex.transporters.models.Place
    private lateinit var destinationLocationPlace: com.bex.transporters.models.Place
    private lateinit var selectedCargo: String
    private lateinit var selectedTruck: String
    private lateinit var selectedPayment: String
    private val client = OkHttpClient()
    private val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    private var userId: Int = 0
    private lateinit var pickupLocationEditText : EditText
    private lateinit var destinationLocationEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_new_demand)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "GoogleApiHere")
        }


        // Assuming you are using Kotlin
        pickupLocationEditText = findViewById<EditText>(R.id.editTextText2)
        destinationLocationEditText = findViewById<EditText>(R.id.editTextText3)

        destinationLocationEditText.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME,  Place.Field.ADDRESS,Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
        pickupLocationEditText.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME,  Place.Field.ADDRESS,Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            startActivityForResult(intent, PICKUP_LOCATION_REQUEST_CODE)
        }

        userId = intent.getIntExtra("user_id", 0)
        Log.i("printing","$userId")
        StrictMode.setThreadPolicy(policy)
        val editTextDimension = findViewById<EditText>(R.id.editTextText)
        val dimensionValue = editTextDimension.text.toString()
        val editTextPickup = findViewById<EditText>(R.id.editTextText2)
        val pickupValue = editTextPickup.text.toString()
        val editTextDestination = findViewById<EditText>(R.id.editTextText3)
        val destinationValue = editTextDestination.text.toString()

        val editTextDate = findViewById<EditText>(R.id.editTextDate2)
        val editTextTime = findViewById<EditText>(R.id.editTextTime)
        editTextDate.setOnClickListener {
            val newFragment = DatePickerFragment(DatePickerDialog.OnDateSetListener { _, year, month, day ->
                editTextDate.setText("$year-${month+1}-$day") // adjust month value because in DatePicker January is represented by 0, not 1.
            })
            newFragment.show(supportFragmentManager, "datePicker")
        }
        editTextTime.setOnClickListener {
            val newFragment = TimePickerFragment(TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                editTextTime.setText("$hourOfDay:$minute:00") // add :00 for seconds
            })
            newFragment.show(supportFragmentManager, "timePicker")
        }

        val spinner1: Spinner = findViewById(R.id.spinner_cargo)
        val spinner2: Spinner = findViewById(R.id.spinner_truck)
        val spinner3: Spinner = findViewById(R.id.spinner_payment)
        val adapter1 = ArrayAdapter.createFromResource(this, R.array.cargo_type, android.R.layout.simple_spinner_item)
        val adapter2 = ArrayAdapter.createFromResource(this, R.array.truck_type, android.R.layout.simple_spinner_item)
        val adapter3 = ArrayAdapter.createFromResource(this, R.array.payment_type, android.R.layout.simple_spinner_item)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.adapter = adapter1
        spinner2.adapter = adapter2
        spinner3.adapter = adapter3

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

                val item = parent.getItemAtPosition(pos)
                selectedCargo = parent.getItemAtPosition(pos).toString()
                Toast.makeText(this@NewDemand, "Selected: $item", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

                val item = parent.getItemAtPosition(pos)
                selectedTruck = parent.getItemAtPosition(pos).toString()
                Toast.makeText(this@NewDemand, "Selected: $item", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                // An item was selected. Retrieve the selected item using

                val item = parent.getItemAtPosition(pos)
                selectedPayment = parent.getItemAtPosition(pos).toString()
                Toast.makeText(this@NewDemand, "Selected: $item", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        val buttonNewDemand = findViewById<Button>(R.id.newdemandformbtn)
        buttonNewDemand.setOnClickListener {
            pickupLocationEditText = findViewById<EditText>(R.id.editTextText2)
            destinationLocationEditText = findViewById<EditText>(R.id.editTextText3)
            val paramBuilder = FormBody.Builder()
            val gson = Gson()
            paramBuilder.add("demand_cargo", selectedCargo)
                .add("demand_dimensions", editTextDimension.text.toString())
                .add("demand_truck_type", selectedTruck)
                .add("demand_pickup_location",gson.toJson(pickupLocationPlace) )//editTextPickup.text.toString()
                .add("demand_destination_location",gson.toJson(destinationLocationPlace) )//editTextDestination.text.toString()
                .add("demand_date", editTextDate.text.toString() + " " + editTextTime.text.toString())
                .add("demand_payment_type", selectedPayment)
                .add("demand_client_id", userId.toString()) // replace with your client id

            val formBody: RequestBody = paramBuilder.build()
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/transporters/demands/addDemands.php")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("NewDemand", "Failed to add demand: ${response.body?.string()}")
                    throw IOException("Unexpected code $response")
                } else {
                    Log.i("NewDemand", "Response body: ${response.body?.string()}")
                }
                runOnUiThread {
                    Toast.makeText(this@NewDemand, "New Demand Added", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@NewDemand, ClientPage::class.java).apply {
                        putExtra("showDemands", true)
                        putExtra("user_id", userId)// Add extra information indicating that we want to show ClientDemands
                    }
                    startActivity(intent)
                }
            }
        }

    }

    class DatePickerFragment(private val listener: DatePickerDialog.OnDateSetListener) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            return DatePickerDialog(requireActivity(), this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            listener.onDateSet(view, year, month, day)
        }
    }

    class TimePickerFragment(private val listener: TimePickerDialog.OnTimeSetListener) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            return TimePickerDialog(requireActivity(), this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            listener.onTimeSet(view, hourOfDay, minute)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICKUP_LOCATION_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val address = place.address ?: "address"
                    val latLng = place.latLng ?: LatLng(0.0, 0.0) // default LatLng
                    pickupLocationPlace = com.bex.transporters.models.Place(place.name, address, latLng, place.id,"0")
                    val editTextPickup = findViewById<EditText>(R.id.editTextText2)
                    editTextPickup.setText(place.name)
                    Log.i("MAP", "Place: ${place.name}, ${place.id}, $address, $latLng")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    status.statusMessage?.let { Log.i("MAP", it) }
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val address = place.address ?: "address"
                    val latLng = place.latLng ?: LatLng(0.0, 0.0) // default LatLng
                    destinationLocationPlace = com.bex.transporters.models.Place(place.name, address, latLng, place.id,"0")
                    val editTextDestination = findViewById<EditText>(R.id.editTextText3)
                    editTextDestination.setText(place.name)
                    Log.i("MAP", "Place: ${place.name}, ${place.id}, $address, $latLng")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    status.statusMessage?.let { Log.i("MAP", it) }
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
        private const val PICKUP_LOCATION_REQUEST_CODE = 1

    }
}

