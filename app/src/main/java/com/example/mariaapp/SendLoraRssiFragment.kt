package com.example.mariaapp

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mariaapp.databinding.FragmentSendLoraRssiBinding
import com.example.mariaapp.databinding.FragmentSendPhoneRssiBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SendLoraRssiFragment : Fragment(), LocationListener {
    private var _binding: FragmentSendLoraRssiBinding? = null
    private val binding get() = _binding!!

    lateinit var latitudeText : String
    lateinit var longitudeText : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendLoraRssiBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)


        binding.buttonSendRSSI.setOnClickListener {
            sendCoordinates()

            // Start a coroutine to execute the delayBeforeFunction()
            CoroutineScope(Dispatchers.Main).launch {
                delayBeforeSendingForm()
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

        binding.textViewCoordinates.text = coordinateText

        println(coordinateText)
    }


    private fun sendCoordinates() {
        if (!::longitudeText.isInitialized or !::latitudeText.isInitialized) {
            println("No coordinates yet")
            return
        }

        val id = binding.editTextMessage.text

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
                            showToast("Location with RSSI Sent")

                        } else {
                            // Handle unsuccessful response
                            showToast("Something went wrong")
                        }
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun sendForm() {
        val formBody = generateFormBody()

        println(formBody)

        val request = Request.Builder()
            .url("$BASE_URL/submitFormLora")
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
                            showToast("Location with RSSI Sent")

                        } else {
                            // Handle unsuccessful response
                            showToast("Something went wrong")
                        }
                    }
                }
            }
        })
    }



    private fun generateFormBody(): FormBody {
        val id = binding.editTextMessage.text

        val name = "Morris pogi"
        val phoneNumber = ""

        val emergency = "[0] + iii"

        val needs = "[0] + iii"

        val dangers = "[0] + iii"

        val numberOfPeople = "0"
        val isImmobile = "0"
        val message = "RSSI of Device to Device: "

        println(id)
        println(name)
        println(phoneNumber)
        println(emergency)
        println(needs)
        println(dangers)
        println(numberOfPeople)
        println(isImmobile)
        println(message)

        return FormBody.Builder()
            .add("clientId", id.toString())
            .add("name", name.toString())
            .add("phoneNumber", phoneNumber.toString())
            .add("emergency", emergency)
            .add("needs", needs)
            .add("dangers", dangers)
            .add("numberOfPeople", numberOfPeople.toString())
            .add("isImmobile", isImmobile)
            .add("message", message.toString())
            .build()
    }

        // Define a suspend function to introduce a delay
        private suspend fun delayBeforeSendingForm() {
            delay(2000) // Delay for 1 second (1000 milliseconds)

            // Call your function here
            sendForm()
        }







}