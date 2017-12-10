package com.shortstack.hackertracker.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Types(
        @SerializedName("event_types")
        var types : Array<Type>,
        @SerializedName("update_date")
        var date : String


) : Serializable

