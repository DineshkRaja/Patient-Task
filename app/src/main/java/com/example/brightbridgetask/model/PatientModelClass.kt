package com.example.brightbridgetask.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class PatientModelClass : RealmObject {
    var patientName: String = ""

    @PrimaryKey
    var patientId: String = ""
    var patientAge: Int = 0
    var patientGender: String = ""
    var patientLocation: PatientLocation? = null
}


open class PatientLocation : RealmObject {
    var patientAddress: String = ""
    var patientLatitude: Double = 0.0
    var patientLongitude: Double = 0.0
}