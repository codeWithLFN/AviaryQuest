package com.example.aviaryquest.models

class User (
    val username: String = "",
    var password: String = "",
    var isMetric: Boolean = true,
    var maxDistance: Double = 0.0
) {
    override fun toString(): String {
        return "$username  $password"
    }
}
