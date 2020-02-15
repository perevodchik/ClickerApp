package com.perevodchik.clickerapp.model

import com.google.gson.annotations.SerializedName

data class Action(@SerializedName("action") var action : String = "tap",
                  @SerializedName("duration") var duration : Long = 500,
                  @SerializedName("enable") var enable : Boolean = true,
                  @SerializedName("id") var id : Long,
                  @SerializedName("loop") var loop : Boolean = false,
                  @SerializedName("patternId") var patternId : Long,
                  @SerializedName("position") var position : Int,
                  @SerializedName("repeat") var repeat : Int = 1,
                  @SerializedName("startDelay") var startDelay : Long = 500,
                  @SerializedName("x") var x : Float = 0f,
                  @SerializedName("x1") var x1 : Float = 0f,
                  @SerializedName("y") var y : Float = 0f,
                  @SerializedName("y1") var y1 : Float = 0f
)