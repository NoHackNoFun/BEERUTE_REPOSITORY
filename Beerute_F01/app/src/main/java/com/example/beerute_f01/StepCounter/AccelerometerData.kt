package com.example.beerute_f01.StepCounter

class AccelerometerData {
    @JvmField
    var value = 0.0
    @JvmField
    var x = 0f
    @JvmField
    var y = 0f
    @JvmField
    var z = 0f
    @JvmField
    var time: Long = 0
    @JvmField
    var isRealPeak = true
}