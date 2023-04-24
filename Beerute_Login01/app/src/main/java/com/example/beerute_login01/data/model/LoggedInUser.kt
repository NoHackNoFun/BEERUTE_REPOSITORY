package com.example.beerute_login01.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String
)