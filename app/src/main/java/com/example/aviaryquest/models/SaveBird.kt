package com.example.aviaryquest.models

import java.util.Date

data class SaveBird(
    val species: String = "",
    val location: String = "",
    val date: Date? = null,
    val time: String = "",
    val notes: String = ""
)
