package com.bex.transporters.pages.driver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bex.transporters.R
import com.bex.transporters.databinding.DriverhomeBinding
import com.bex.transporters.pages.client.ClientDemands
import com.bex.transporters.pages.client.ClientOffers
import com.bex.transporters.pages.client.ClientProfile


class DriverPage : AppCompatActivity()  {
    private lateinit var binding : DriverhomeBinding
    private var driverId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driverhome)
        val sharedPreferences =getSharedPreferences("UserTypePrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        var userID = sharedPreferences.getInt("userID",0)
       // driverId = intent.getIntExtra("driver_id", 0)

        driverId = userID
        binding = DriverhomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
       /* val showDemands = intent.getBooleanExtra("showDemands", false)
        if (showDemands) {
            replaceFragment(DriverDemands.newInstance(driverId))
        } else {
            replaceFragment(DriverOffers())
        }
*/
        replaceFragment(DriverDemands())
        binding.bottomNavigationView3.setOnItemSelectedListener {
            when(it.itemId){
                R.id.offersdriver -> replaceFragment(DriverOffers.newInstance(driverId))
                R.id.demandsdriver -> replaceFragment(DriverDemands.newInstance(driverId))
                R.id.profiledriver -> replaceFragment(DriverProfile())
                R.id.transportsdriver -> replaceFragment(DriverTransports())

                else -> {}
            }
            true }

    }
    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.driver_frame_layout,fragment)
        fragmentTransaction.commit()
    }



}