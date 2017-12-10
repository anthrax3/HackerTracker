package com.shortstack.hackertracker.Fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.*
import com.shortstack.hackertracker.Alert.MaterialAlert
import com.shortstack.hackertracker.Analytics.AnalyticsController
import com.shortstack.hackertracker.Application.App
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.View.UberView
import kotlinx.android.synthetic.main.fragment_maps.*

class MapsFragment : Fragment() {


    override fun onCreateView(inflater : LayoutInflater?, container : ViewGroup?, savedInstanceState : Bundle?) : View? {
        val view = inflater!!.inflate(R.layout.fragment_maps, container, false)
        setHasOptionsMenu(true)
        return view
    }


    override fun onViewCreated(view : View?, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PagerAdapter(activity.supportFragmentManager)
        pager.adapter = adapter

//        if (App.application.databaseController.databaseName != Constants.DEFCON_DATABASE_NAME) {
//            tab_layout.visibility = View.GONE
//        }
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.map_day_title)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.map_night_title)))
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab : TabLayout.Tab?) {}
            override fun onTabUnselected(tab : TabLayout.Tab?) {}
            override fun onTabSelected(tab : TabLayout.Tab?) {
                pager.currentItem = tab!!.position
            }

        })
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean {
        when (item!!.itemId) {
            R.id.action_uber -> {
                App.Companion.application.analyticsController.tagCustomEvent(AnalyticsController.Analytics.UBER)
                MaterialAlert.create(context).setTitle(com.shortstack.hackertracker.R.string.uber).setView(UberView(context)).show()
                return true
            }
        }

        return false
    }

    override fun onDestroyView() {
        (pager.adapter as PagerAdapter).destroy()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu : Menu?, inflater : MenuInflater?) {
        inflater!!.inflate(R.menu.maps, menu)
//        if (App.application.databaseController.databaseName != Constants.DEFCON_DATABASE_NAME) {
//            menu?.removeItem(R.id.action_uber)
//        }
    }

    class PagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {

        var maps : Array<Fragment>

        init {
//            if (App.application.databaseController.databaseName == Constants.DEFCON_DATABASE_NAME) {
                maps = arrayOf(MapFragment.newInstance(MAP_DAY),
                        MapFragment.newInstance(MAP_NIGHT))
//            } else {
//                maps = arrayOf(MapFragment.newInstance(TOORCON))
//            }
        }

        override fun getItem(position : Int) : Fragment {
            return maps[position]
        }


        override fun getCount() : Int {
            return maps.size
        }

        fun destroy() {
            maps.iterator().forEach { it.onDestroyView() }
        }

    }

    companion object {

        val MAP_DAY = "dc-25-floorplan-v8-final-public.pdf"
        val MAP_NIGHT = "dc-25-floorplan-v7.6-night.pdf"
        val TOORCON = "toorcon-19-map.pdf"

        fun newInstance() : Fragment {
            return MapsFragment()
        }
    }
}
