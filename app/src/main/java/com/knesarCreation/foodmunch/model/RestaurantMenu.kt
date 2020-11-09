package com.knesarCreation.foodmunch.model

data class RestaurantMenu(
    val id: Int,
    val name: String,
    val costForOne: String,
    val resId: Int
) {
    /*empty constructor*/
     constructor() : this(0, "", "", 0)
}