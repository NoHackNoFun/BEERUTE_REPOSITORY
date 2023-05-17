package com.example.beerute_f00.ui.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Record Fragment"
    }
    val text: LiveData<String> = _text
}