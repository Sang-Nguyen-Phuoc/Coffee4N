package com.example.coffee4n.ui.profile

data class ProfileState (
    val showEditDialog : Boolean = false,

    val id : Int = 0,
    val email : String = "",
    val username : String = "",
    val name : String = "",
    val phone : String = "",
    val address : String = "",
)
