package com.example.mariaapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import com.example.mariaapp.databinding.FragmentFirstBinding
import com.example.mariaapp.databinding.FragmentLandingBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class LandingFragment : Fragment(), LocationListener {
    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)




        testConnection()

        binding.sosbutton.visibility = View.INVISIBLE

        binding.sosbutton.setOnClickListener {
            // Generate and store ID if needed
            if (!IDManager.isIDValid(requireContext())) {
                IDManager.generateAndStoreID(requireContext())
            }

            // Retrieve the stored ID and expiration time
            val (id, expirationTime) = IDManager.getStoredID(requireContext())

            println(id)
            findNavController().navigate(R.id.action_landingFragment_to_sendSosFragment)
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()

        val activity = requireActivity() as MainActivity
        activity.unregisterLocationListener(this)
    }

    override fun onResume() {
        super.onResume()

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)

        val lastKnownLocation = activity.getLastKnownLocation()
        if (lastKnownLocation != null) {
            updateUIWithLocation(lastKnownLocation)
        }
    }



    override fun onLocationChanged(location: Location) {
            updateUIWithLocation(location)
    }

    private fun updateUIWithLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        val coordinateText = "$latitude,$longitude"


        binding.textHomePageLocation.text = coordinateText
    }



    private fun testConnection(){
        val request = Request.Builder()
            .url(testConnectionURL)
            .build()

        HttpClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                retryRequest()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        retryRequest()
                        throw IOException("Unexpected code $response")
                    }

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())

                    activity?.runOnUiThread {
                        binding.textConnected.text = getString(R.string.connected_string)
                        binding.sosbutton.visibility = View.VISIBLE
                    }


                }
            }
        })
    }

    fun retryRequest() {
        val delayMillis = 5000L // Delay in milliseconds before retrying

        // Wait for the specified delay and then send the request again
        Thread.sleep(delayMillis)
        testConnection()
    }



}