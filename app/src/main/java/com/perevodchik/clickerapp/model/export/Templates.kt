package com.perevodchik.clickerapp.model.export

import com.google.gson.annotations.SerializedName
import com.perevodchik.clickerapp.model.Action

data class Templates (
	@SerializedName("id") val id : Int,
	@SerializedName("name") val name : String,
	@SerializedName("actions") val actions : List<Action>,
	@SerializedName("working_time") val patternExecuteDuration: Long
)