package com.perevodchik.clickerapp.model

data class Pattern (
    var id: Long = -1,
    var name: String = "",
    val actions: MutableList<Action> = mutableListOf(),
    val executeTime: Long = 1
)