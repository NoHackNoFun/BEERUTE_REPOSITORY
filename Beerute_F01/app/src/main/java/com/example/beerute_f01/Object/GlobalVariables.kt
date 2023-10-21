package com.example.beerute_f01.Object

import com.example.beerute_f01.RankingUserClass

object GlobalVariables {

    var userEmail: String = ""
    var userProvider: String = ""

    var selectedRouteId: String = ""
    var selectedPlace: String = ""
    var selectedSteps: Int = 0
    var selectedKm: Double = 0.0
    var selectedTime: Double = 0.0
    var selectedUser: String = ""
    var selectedBestUser: String = ""

    var oldlatitude: Double = 0.0
    var oldlongitude: Double = 0.0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var lastid: Int = 0

    var userRankingList: MutableList<RankingUserClass> = mutableListOf()
    var i: Int = 0
    var kmmax: Double = 0.0
    var legend: String = ""

    var aux: Double = 0.0
}