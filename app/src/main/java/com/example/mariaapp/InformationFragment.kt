package com.example.mariaapp

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.navigation.fragment.findNavController
import com.example.mariaapp.databinding.FragmentInformationBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException


class InformationFragment : Fragment(), LocationListener{
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!

    lateinit var latitudeText : String
    lateinit var longitudeText : String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.registerLocationListener(this)

        binding.buttonSendSOS.setOnClickListener {
            sendCoordinates()
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
            .add("isSafe", "0")
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
                            // Perform navigation when the response is successful
                            sendForm()
                        } else {
                            // Handle unsuccessful response
                        }
                    }
                }
            }
        })
    }

    private fun sendForm() {
        val formBody = generateFormBody()

        println(formBody)

        val request = Request.Builder()
            .url("$BASE_URL/submitForm")
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
                            // Perform navigation when the response is successful
                            if (bodyString != null) {
                                goToUpdateFragment(bodyString)
                            }
                        } else {
                            // Handle unsuccessful response
                        }
                    }
                }
            }
        })
    }



    private fun generateFormBody(): FormBody {
        // Generate and store ID if needed
        if (!IDManager.isIDValid(requireContext())) {
            IDManager.generateAndStoreID(requireContext())
        }

        // Retrieve the stored ID and expiration time
        val (id, expirationTime) = IDManager.getStoredID(requireContext())

        val name = binding.editTextName.text
        val phoneNumber = binding.editTextPhoneNumber.text

        val emergency = "[" +
                "${checkBoxStatus(binding.checkBoxMedical)}, " +
                "${checkBoxStatus(binding.checkBoxFire)} ," +
                "${checkBoxStatus(binding.checkBoxLandSlide)} ," +
                "${checkBoxStatus(binding.checkBoxTrapped)} ," +
                "${checkBoxStatus(binding.checkBoxLost)}] + " +
                "${binding.editTexttextOthersEmergency.text}"

        val needs = "[" +
                "${checkBoxStatus(binding.checkBoxFirstAid)}, " +
                "${checkBoxStatus(binding.checkBoxShelter)} ," +
                "${checkBoxStatus(binding.checkBoxFoodWater)} ," +
                "${checkBoxStatus(binding.checkBoxRescue)}] + " +
                "${binding.editTexttextOthersNeeds.text}"

        val dangers = "[" +
                "${checkBoxStatus(binding.checkBoxBlockedTrail)}, " +
                "${checkBoxStatus(binding.checkBoxExplosives)} ," +
                "${checkBoxStatus(binding.checkBoxFireSmoke)} ," +
                "${checkBoxStatus(binding.checkBoxWildAnimals)} ," +
                "${checkBoxStatus(binding.checkBoxFallenTrees)} ," +
                "${checkBoxStatus(binding.checkBoxChemicals)}] + " +
                "${binding.editTextOthersDangers.text}"

        val numberOfPeople = binding.editTextNumberOfPeople.text
        val isImmobile = checkBoxStatus(binding.checkBoxImmobile)
        val message = binding.editTexttextMessage.text

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

    private fun checkBoxStatus(checkBox: CheckBox): String {
        if (checkBox.isChecked) {
            return "1"
        }

        return "0"
    }

    private fun goToUpdateFragment(messageID: String) {
        val action = InformationFragmentDirections.actionInformationFragmentToUpdateFragment(messageID)
        findNavController().navigate(action)
    }


}