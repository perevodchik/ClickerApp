package com.perevodchik.clickerapp.model.pattern

import com.google.gson.annotations.SerializedName

data class Actions (

    @SerializedName("action") val action : String,
    @SerializedName("duration") val duration : Int,
    @SerializedName("enable") val enable : Boolean,
    @SerializedName("id") val id : Int,
    @SerializedName("loop") val loop : Boolean,
    @SerializedName("patternId") val patternId : Int,
    @SerializedName("position") val position : Int,
    @SerializedName("repeat") val repeat : Int,
    @SerializedName("startDelay") val startDelay : Int,
    @SerializedName("x") val x : Int,
    @SerializedName("x1") val x1 : Int,
    @SerializedName("y") val y : Int,
    @SerializedName("y1") val y1 : Int
)