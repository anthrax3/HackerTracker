package com.shortstack.hackertracker

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.Application.App
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

inline fun FragmentManager.inTransaction(func : FragmentTransaction.() -> Unit) {
    val fragmentTransaction = beginTransaction()
    fragmentTransaction.func()
    fragmentTransaction.commit()
}

fun FragmentManager.contains(tag : String) = findFragmentByTag(tag) != null

fun AppCompatActivity.addFragment(fragment : Fragment, title : String, tag : String, frameId : Int) {
    supportActionBar?.title = title
    supportFragmentManager.inTransaction { add(frameId, fragment, tag) }
    //invalidateOptionsMenu()
}

fun AppCompatActivity.replaceFragment(fragment : Fragment, title : String, tag : String, frameId : Int) {
    supportActionBar?.title = title
    supportFragmentManager.inTransaction { replace(frameId, fragment, tag) }
    //invalidateOptionsMenu()
}

fun Date.isToday() : Boolean {
    val current = App.getCurrentCalendar()

    val cal = Calendar.getInstance()
    cal.time = this

    return cal.get(Calendar.YEAR) == current.get(Calendar.YEAR)
            && cal.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)
}

fun Date.isTomorrow() : Boolean {
    val cal1 = App.getCurrentCalendar()
    cal1.roll(Calendar.DAY_OF_YEAR, true)

    val cal2 = Calendar.getInstance()
    cal2.time = this

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun Date.isSoonish(SOON_DAYS_AMOUNT : Int) : Boolean {
    val cal1 = App.getCurrentCalendar()

    val cal2 = Calendar.getInstance()
    cal2.time = this
    cal2.roll(Calendar.DAY_OF_YEAR, SOON_DAYS_AMOUNT)

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) <= cal2.get(Calendar.DAY_OF_YEAR)
}

fun Date.getDateDifference(date : Date, timeUnit : TimeUnit) : Long {
    return timeUnit.convert(date.time - this.time, TimeUnit.MILLISECONDS);
}

@SuppressLint("SimpleDateFormat")
fun Calendar.format8601() : String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(this.time)

fun <T> kotlin.Array<out T>.joinSQLOr() : String {
    return joinToString(prefix = " (", separator = " OR ", postfix = ") ")
}

fun String.concat(text : String) : String {
    if (this.isNullOrEmpty())
        return text
    return this + text
}

fun <T> Gson.fromJsonFile(filename : String, type : Class<T>) : T {
    try {
        val s = "database/DEFCON25/$filename"
        val stream = App.application.assets.open(s)

        val size = stream.available()

        val buffer = ByteArray(size)

        stream.read(buffer)
        stream.close()

        return fromJson(String(buffer), type)

    } catch (e : IOException) {
        Logger.e(e, "Could not create the database.")
        throw e
    }
}