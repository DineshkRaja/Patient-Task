package com.example.brightbridgetask.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brightbridgetask.R
import com.example.brightbridgetask.databinding.ActivityAddPatientBinding
import com.example.brightbridgetask.model.PatientLocation
import com.example.brightbridgetask.model.PatientModelClass
import com.google.android.material.textview.MaterialTextView
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults


class AddPatientActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var bindingForAddPatient: ActivityAddPatientBinding
    private var genderSpinner: String = ""
    private var realm: Realm? = null
    private var configuration: RealmConfiguration? = null
    var addresses: MutableList<ModelAddress>? = null

    inner class ModelAddress() {
        var address: String? = null
        var lat: Double? = null
        var long: Double? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingForAddPatient = ActivityAddPatientBinding.inflate(layoutInflater)
        val view = bindingForAddPatient.root
        setContentView(view)

        setSupportActionBar(bindingForAddPatient.IdToolbar.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Add New Patients"

        bindingForAddPatient.IdAddPatientButton.setOnClickListener(this)
        /*if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "", Locale.ENGLISH)
        }
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.IdAddPatientButton) as AutocompleteSupportFragment?
        autocompleteFragment!!.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.requireView()
            .findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_button).visibility = View.GONE

        (autocompleteFragment.requireView().findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).textSize = 16f

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (place.latLng != null) {

                }
            }

            override fun onError(p0: Status) {

            }
        })*/

        addresses = mutableListOf()
        addresses?.add(ModelAddress().apply {
            address = "Radhika Avenue,P N Pudur, Coimbatore, Tamil Nadu"
            lat = 11.022148900459698
            long = 76.92073217984405
        })
        addresses?.add(ModelAddress().apply {
            address =
                "Kurumbapalayam is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India"
            lat = 11.1132
            long = 77.0277
        })
        addresses?.add(ModelAddress().apply {
            address =
                "Mettupalayam is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India, 621210"
            lat = 11.2984
            long = 76.9359
        })

        addresses?.add(ModelAddress().apply {
            address =
                "Neelambur is an locality in Coimbatore, Tiruppur District, Tamil Nadu, India, 641062"
            lat = 11.0635
            long = 77.0894
        })


        //addPatientData("HRS0001","Dinesh","Male",25,"Keeranatham is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India, 641035",11.1169,76.9978)


        val addressName: MutableList<String> = mutableListOf()
        addresses?.forEach {
            addressName.add(it.address ?: "")
        }

        //val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.simple_dropdown_item_1line, ADDRESS)
        bindingForAddPatient.IdPatientAddress.setAdapter(
            ArrayAdapter(
                this@AddPatientActivity,
                android.R.layout.simple_dropdown_item_1line,
                addressName
            )
        )
        bindingForAddPatient.IdPatientAddress.threshold = 0

        ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bindingForAddPatient.IdPatientAgeSpinner.adapter = adapter
        }

        bindingForAddPatient.IdPatientAgeSpinner.onItemSelectedListener =
            object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    genderSpinner = (p1 as MaterialTextView).text.toString() ?: ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.IdAddPatientButton -> {
                if (validation()) {
                    configuration = RealmConfiguration.create(
                        schema = setOf(
                            PatientModelClass::class,
                            PatientLocation::class
                        )
                    )
                    realm = Realm.open(configuration!!)

                    val items: RealmResults<PatientModelClass> =
                        realm!!.query<PatientModelClass>().find()

                    val patientObject = PatientModelClass().apply {
                        patientId = "HRS000" + (items.size + 1).toString()
                        patientName = bindingForAddPatient.IdPatientName.text.toString() ?: ""
                        patientGender = genderSpinner
                        patientAge = bindingForAddPatient.IdPatientAge.text.toString().toInt()

                        patientLocation = PatientLocation().apply {
                            patientAddress = findSelectedAddress()?.address.toString()
                            patientLatitude = findSelectedAddress()?.lat!!
                            patientLongitude = findSelectedAddress()?.long!!
                        }

                    }

                    // Persist it in a transaction
                    realm?.writeBlocking { // this : MutableRealm
                        copyToRealm(patientObject)
                    }
                    Toast.makeText(
                        this@AddPatientActivity,
                        "Successfully Patient added",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun findSelectedAddress(): ModelAddress? {
        for (item in addresses!!) {
            if (bindingForAddPatient.IdPatientAddress.text.toString() == item.address) {
                return item
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }

    private fun validation(): Boolean {
        if (bindingForAddPatient.IdPatientName.text.isNullOrEmpty()) {
            bindingForAddPatient.IdPatientName.error = "Please enter the patient name"
            return false
        } else if (bindingForAddPatient.IdPatientAge.text.isNullOrEmpty()) {
            bindingForAddPatient.IdPatientAge.error = "Please enter the patient age"
            return false
        } else if (genderSpinner.isNullOrEmpty()) {
            Toast.makeText(
                this@AddPatientActivity,
                "Please select the Patient Age",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (bindingForAddPatient.IdPatientAddress.text.isNullOrEmpty()) {
            bindingForAddPatient.IdPatientAddress.error = "Please enter the Patient address"
            return false
        }
        return true
    }
}