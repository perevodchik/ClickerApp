package com.perevodchik.clickerapp.model.pattern

import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("link") val link: String,
	@SerializedName("clicker_info") val clicker_info: ClickerInfo,
	@SerializedName("working_time") val patternExecuteDuration: Long
)