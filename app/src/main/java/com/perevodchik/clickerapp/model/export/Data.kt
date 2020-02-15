package com.perevodchik.clickerapp.model.export

import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("type") val type : String,
	@SerializedName("templates") val templates : List<Templates>
)