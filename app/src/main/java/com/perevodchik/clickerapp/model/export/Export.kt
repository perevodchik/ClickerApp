package com.perevodchik.clickerapp.model.export

import com.google.gson.annotations.SerializedName

data class Export (
	@SerializedName("data") val data : Data
)