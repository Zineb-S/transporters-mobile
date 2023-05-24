package com.bex.transporters.pages.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bex.transporters.R
import com.bex.transporters.databinding.ClienthomeBinding


class ClientPage : AppCompatActivity()  {
    private lateinit var binding : ClienthomeBinding
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        //userId = intent.getIntExtra("user_id", 0)

        super.onCreate(savedInstanceState)
        val sharedPreferences =getSharedPreferences("UserTypePrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        var userID = sharedPreferences.getInt("userID",0)
        userId = userID
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon)
        binding = ClienthomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val showDemands = intent.getBooleanExtra("showDemands", false)
        if (showDemands) {
            replaceFragment(ClientDemands.newInstance(userId))
        } else {
            replaceFragment(ClientOffers.newInstance(userId))
        }
        replaceFragment(ClientOffers.newInstance(userId))

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.offersclient -> replaceFragment(ClientOffers.newInstance(userId))
                R.id.demandsclient -> replaceFragment(ClientDemands.newInstance(userId))
                R.id.profileclient -> replaceFragment(ClientProfile.newInstance(userId))

                else -> {}
            }
            true }

    }
    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.client_frame_layout,fragment)
        fragmentTransaction.commit()
    }



}