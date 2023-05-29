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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.mariaapp.databinding.FragmentLandingBinding
import com.example.mariaapp.databinding.FragmentSendSosBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SendSosFragment : Fragment(), LocationListener {
    private var _binding: FragmentSendSosBinding? = null
    private val binding get() = _binding!!

    lateinit var latitudeText : String
    lateinit var longitudeText : String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendSosBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)


        binding.sendsosbutton.setOnClickListener {
            sendCoordinates()
        }

        binding.adddetailsbutton.setOnClickListener {
            findNavController().navigate(R.id.action_sendSosFragment_to_informationFragment)
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

        longitudeText = longitude.toString()
        latitudeText = latitude.toString()

        val coordinateText = "$latitude,$longitude"

        binding.textHomePageLocation.text = coordinateText


        println(coordinateText)
    }


    private fun sendCoordinates() {
        if (!::longitudeText.isInitialized or !::latitudeText.isInitialized) {
            println("No coordinates yet")
            return
        }

        // Generate and store ID if needed
        if (!IDManager.isIDValid(requireContext())) {
            IDManager.generateAndStoreID(requireContext())
        }

        // Retrieve the stored ID and expiration time
        val (id, expirationTime) = IDManager.getStoredID(requireContext())

        println(id)
        val formBody = FormBody.Builder()
            .add("clientId", id.toString())
            .add("longitude", longitudeText)
            .add("latitude", latitudeText)
            .build()
        val request = Request.Builder()
            .url("$BASE_URL/sendLocation")
            .post(formBody)
            .build()

        HttpClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {


                    activity?.runOnUiThread {
                        if (response.isSuccessful) {
                            println("sent")
                            // Perform navigation when the response is successful
                            goToUpdateFragment()
                        } else {
                            // Handle unsuccessful response
                        }
                    }
                }
            }
        })
    }

    private fun goToUpdateFragment() {
        findNavController().navigate(R.id.action_sendSosFragment_to_updateFragment)
    }

    fun retryRequest() {
        val delayMillis = 5000L // Delay in milliseconds before retrying

        // Wait for the specified delay and then send the request again
        Thread.sleep(delayMillis)
        sendCoordinates()
    }
}