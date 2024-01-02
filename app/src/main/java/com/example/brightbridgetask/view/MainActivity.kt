package com.example.brightbridgetask.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.brightbridgetask.R
import com.example.brightbridgetask.databinding.ActivityMainBinding
import com.example.brightbridgetask.databinding.ItemPatientViewBinding
import com.example.brightbridgetask.model.PatientLocation
import com.example.brightbridgetask.model.PatientModelClass
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var bindingForHome: ActivityMainBinding
    private var realm: Realm? = null
    private var patientListAdapter: PatientListAdapter? = null
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingForHome = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingForHome.root

        setSupportActionBar(bindingForHome.IdToolbar.toolbar)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Patients List"

        setContentView(view)
        onClickListeners()
        val configuration = RealmConfiguration.create(
            schema = setOf(
                PatientModelClass::class,
                PatientLocation::class
            )
        )
        realm = Realm.open(configuration)

        val items: RealmResults<PatientModelClass> = realm!!.query<PatientModelClass>().find()

        if (items.isEmpty()) {
            addPatientData(
                "HRS0001",
                "Dinesh",
                "Male",
                25,
                "Keeranatham is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India, 641035",
                11.1169,
                76.9978
            )
            addPatientData(
                "HRS0002",
                "Kumar",
                "Male",
                26,
                "Alanthurai is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India, 641101.",
                10.9528,
                76.7886
            )
            addPatientData(
                "HRS0003",
                "Karthi",
                "Male",
                28,
                "Bodipalayam is an locality in Coimbatore, Coimbatore District, Tamil Nadu, India, 641105.",
                10.9014,
                76.9758
            )
            addPatientData(
                "HRS0004",
                "Raja",
                "Male",
                20,
                "Kovilpalayam is an locality in Coimbatore, Tiruppur District, Tamil Nadu, India, 642110.",
                11.1418,
                77.0439
            )
        }


        job = CoroutineScope(Dispatchers.Main).launch {
            val itemsFlow = items.asFlow()
            itemsFlow.collect { changes: ResultsChange<PatientModelClass> ->
                when (changes) {
                    // UpdatedResults means this change represents an update/insert/delete operation
                    is UpdatedResults -> {
                        changes.insertions // indexes of inserted objects
                        changes.insertionRanges // ranges of inserted objects
                        changes.changes // indexes of modified objects
                        changes.changeRanges // ranges of modified objects
                        changes.deletions // indexes of deleted objects
                        changes.deletionRanges // ranges of deleted objects
                        changes.list // the full collection of objects
                        patientListAdapter?.setItems(changes.list.toMutableList())

                    }

                    else -> {
                        // types other than UpdatedResults are not changes -- ignore them
                    }
                }
            }
        }


        patientListAdapter = PatientListAdapter(items.toMutableList(), this@MainActivity)
        bindingForHome.IdPatientRecyclerView.adapter = patientListAdapter
        bindingForHome.IdPatientRecyclerView.layoutManager =
            LinearLayoutManager(this@MainActivity, VERTICAL, false)

    }

    private fun onClickListeners() {
        bindingForHome.IdAddNewPatient.setOnClickListener(this)
    }

    private fun addPatientData(
        patientIdValue: String,
        patientNameValue: String,
        patientGenderValue: String,
        patientAgeValue: Int,
        patientAddressValue: String,
        patientLatitudeValue: Double,
        patientLongitudeValue: Double
    ) {
        val patientObject = PatientModelClass().apply {
            patientId = patientIdValue
            patientName = patientNameValue
            patientGender = patientGenderValue
            patientAge = patientAgeValue
            patientLocation = PatientLocation().apply {
                patientAddress = patientAddressValue
                patientLatitude = patientLatitudeValue
                patientLongitude = patientLongitudeValue
            }
        }

        // Persist it in a transaction
        realm?.writeBlocking { // this : MutableRealm
            copyToRealm(patientObject)
        }

        /*// Asynchronous updates with Kotlin coroutines
        CoroutineScope(Dispatchers.Main).async {
            realm.write { // this : MutableRealm
                val managedPerson = copyToRealm(person)
            }
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        realm?.close()
    }

    class PatientListAdapter(
        dataSet: MutableList<PatientModelClass>,
        private var activity: Activity
    ) : RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {

        private var patientList: MutableList<PatientModelClass> = mutableListOf()

        init {
            this.patientList = dataSet
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItems(dataSet: MutableList<PatientModelClass>) {
            this.patientList.clear()
            this.patientList.addAll(dataSet)
            notifyDataSetChanged()
        }

        inner class ViewHolder(val binding: ItemPatientViewBinding) :
            RecyclerView.ViewHolder(binding.root)


        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

            val binding = ItemPatientViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
            return ViewHolder(binding)
        }


        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            with(viewHolder) {
                with(patientList[position]) {
                    binding.IdPatientName.text = patientName
                    binding.IdPatientIdentity.text = patientId
                    binding.IdPatientAge.text = patientAge.toString()
                    binding.IdPatientGender.text = patientGender
                    binding.IdPatientAddress.text = patientLocation?.patientAddress ?: "-"

                    binding.IdPatientAddress.setOnClickListener {
                        redirectToMap(
                            patientLocation!!.patientLatitude,
                            patientLocation!!.patientLongitude
                        )
                    }

                }
            }

        }


        override fun getItemCount() = patientList.size

        @SuppressLint("QueryPermissionsNeeded")
        fun redirectToMap(latitude: Double, longitude: Double) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")
            )
            intent.setPackage("com.google.android.apps.maps")
            activity.startActivity(intent)
        }

    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.IdAddNewPatient -> {
                val intent = Intent(this@MainActivity, AddPatientActivity::class.java)
                startActivity(intent)
            }
        }
    }


}