package com.knesarCreation.foodmunch.model

data class OrderHistory(
    val orderId: Int,
    val resName: String,
    val totalCost: String,
    val orderDate: String,
    val foodItem: List<FoodItems>
) {
    constructor() : this(0, "", "", "", listOf())
}