package com.shortstack.hackertracker.models

import android.content.ContentValues
import android.text.TextUtils
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.App
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Item : Serializable {

    @SerializedName("index") val index : Int = 0
    @SerializedName("entry_type") val type : String? = null
    @SerializedName("title")val title : String? = null
    @SerializedName("who") val speakers : Array<Speaker>? = null


    @SerializedName("description") val description : String? = null

    @SerializedName("start_date") val begin : String? = null
    @SerializedName("end_date") val end : String? = null

    @SerializedName("location")val location : String? = null
    @SerializedName("link")val link : String? = null
    private val dctvChannel : String? = null
    @SerializedName("includes")private val includes : String? = null

    @SerializedName("updated_at") var updatedAt : String = ""


    // State
    //private int isNew;
    @SerializedName("bookmarked") private var isBookmarked : Int = 0

    val date : String
        get() = begin!!.substring(0, 11)


    // State
    val isTool : Boolean
        get() = includes != null && includes.contains("Tool")

    val isExploit : Boolean
        get() = includes != null && includes.contains("Exploit")

    val isDemo : Boolean
        get() = includes != null && includes.contains("Demo")

    fun isBookmarked() : Boolean {
        return isBookmarked == BOOKMARKED
    }


    // Date
    private val dateFormat : DateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")


    private fun getDateObject(dateStr : String?) : Date {
        val readFormat = dateFormat

        var date : Date = App.getCurrentDate()
        try {
            date = readFormat.parse(dateStr)
        } catch (e : ParseException) {
            e.printStackTrace()
        }

        return date
    }

    val beginDateObject : Date by lazy { getDateObject(begin) }

    val endDateObject : Date by lazy { getDateObject(end) }

    fun hasExpired() : Boolean {
        val date = App.getCurrentDate()
        val end = endDateObject

        if (end == null || date == null) {
            Logger.e("Cannot check if item is expired. Something is null.")
            val core = Crashlytics.getInstance().core
            if (core != null) {
                core.log("Could not check if item is expired, some date is null.")
                core.log("Date null? == " + (date == null) + ", End null? == " + (end == null))
                core.logException(NullPointerException())
            }
            return false
        }

        return date.after(end)
    }

    fun hasBegin() : Boolean {
        val date = App.getCurrentDate()
        return date.after(beginDateObject)
    }

    private val calendar : Calendar?
        get() {
            if (TextUtils.isEmpty(begin) || TextUtils.isEmpty(date)) {
                return null
            }

            val calendar = Calendar.getInstance()
            calendar.time = beginDateObject

            return calendar
        }

    val notificationTime : Int
        get() {
            val current = Calendar.getInstance()
            val calendar = calendar ?: return 0
            return ((calendar.timeInMillis - current.timeInMillis) / 1000).toInt()
        }

    val dateStamp : String
        get() {
            val date = beginDateObject
            return getDateStamp(date)
        }

    //

    fun getContentValues(gson : Gson) : ContentValues {
        val values = ContentValues()

        val json = gson.toJson(this)
        try {
            val `object` = JSONObject(json)

            val keys = `object`.keys()
            var key : String
            while (keys.hasNext()) {
                key = keys.next()
                if (key != "bookmarked")
                    values.put(key, `object`.getString(key))
            }

            values.remove("end_date_object\$delegate")
            values.remove("begin_date_object\$delegate")

        } catch (e : JSONException) {
            e.printStackTrace()
        }

        return values
    }


    // Actions / Modifiers

    private fun setBookmarked() {
        isBookmarked = BOOKMARKED
    }

    private fun setUnbookmarked() {
        isBookmarked = UNBOOKMARKED
    }

    fun toggleBookmark() {
        if (isBookmarked()) {
            setUnbookmarked()
        } else {
            setBookmarked()
        }
    }

    override fun toString() : String {
        return "{ id: $index, title: \"$title\", location: \"$location\", \"type: $type\" }"
    }

    companion object {

        internal val BOOKMARKED = 1
        internal val UNBOOKMARKED = 0

        fun getDateStamp(date : Date?) : String {
            if (date == null) return ""

            return App.getRelativeDateStamp(date)
        }
    }
}
