package com.perevodchik.clickerapp.model.pattern

import com.google.gson.annotations.SerializedName
import com.perevodchik.clickerapp.model.Action

data class ClickerInfo (
	@SerializedName("id") val id : Int,
	@SerializedName("name") val name : String,
	@SerializedName("actions") val actions : List<Action>
)