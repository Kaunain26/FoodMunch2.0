package com.knesarCreation.foodmunch.model

data class Users(
    val userName: String,
    val email: String,
    val mobileNo: String,
    val deliveryAddress: String,
    val password: String
) {
    constructor() : this("", "", "", "", "")
}