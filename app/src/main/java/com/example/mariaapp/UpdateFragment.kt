package com.example.mariaapp

import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mariaapp.databinding.FragmentUpdateBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class UpdateFragment : Fragment(), LocationListener {
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args: UpdateFragmentArgs by navArgs()

    lateinit var latitudeText : String
    lateinit var longitudeText : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)


        binding.textViewMessageID.text = getMessageID()
        binding.textViewDateTimeSent.text = getCurrentDateTimeString()

        binding.sendupdatebutton.setOnClickListener {
            findNavController().navigate(R.id.action_updateFragment_to_informationFragment)
        }

        binding.homebutton.setOnClickListener {
            sendCoordinates()
        }

        binding.homebutton.visibility = View.GONE

        binding.checkBoxNeedHelp.setOnCheckedChangeListener { _, isChecked  ->
            if (isChecked) {
                binding.homebutton.visibility = View.VISIBLE
            } else {
                binding.homebutton.visibility = View.GONE
            }
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
            .add("isSafe", "1")
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
                val responseBody: ResponseBody? = response.body
                val bodyString = responseBody?.string()

                response.use {


                    activity?.runOnUiThread {
                        if (response.isSuccessful) {

                            println("sent")
                            println(bodyString)

                            // Perform navigation when the response is successful
                            if (bodyString != null) {
                                goHome()
                            }
                        } else {
                            // Handle unsuccessful response
                        }
                    }
                }
            }
        })
    }

    private fun getMessageID(): String {
        val jsonObject = JSONObject(args.messageID)

        // Get the value of "muid" field
        val muid = jsonObject.optString("muid")

        return "Your message ID: ${muid}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateTimeString(): String {
        val currentDateTime = LocalDateTime.now()

        // Define the desired date-time format
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy @ h:mm a", Locale.ENGLISH)

        // Format the current date-time using the formatter
        return currentDateTime.format(formatter)
    }

    private fun goHome() {
        findNavController().navigate(R.id.action_updateFragment_to_landingFragment)
    }

}