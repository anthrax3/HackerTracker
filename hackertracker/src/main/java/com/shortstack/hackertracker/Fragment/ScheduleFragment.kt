package com.shortstack.hackertracker.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.Application.App
import com.shortstack.hackertracker.Common.Constants
import com.shortstack.hackertracker.Event.RefreshTimerEvent
import com.shortstack.hackertracker.Event.UpdateListContentsEvent
import com.shortstack.hackertracker.List.ListViewsInterface
import com.shortstack.hackertracker.List.ScheduleInfiniteScrollListener
import com.shortstack.hackertracker.List.ScheduleItemAdapter
import com.shortstack.hackertracker.Network.DatabaseService
import com.shortstack.hackertracker.Network.FullResponse
import com.shortstack.hackertracker.Network.SyncRepository
import com.shortstack.hackertracker.R
import com.squareup.otto.Subscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.fragment_schedule.view.*
import java.util.*


class ScheduleFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, ListViewsInterface {


    lateinit var adapter : ScheduleItemAdapter


    var mHandler : Handler = object : Handler() {
        override fun handleMessage(msg : Message) {
            App.application.postBusEvent(RefreshTimerEvent())
            if (adapter != null) {
                adapter.notifyTimeChanged()
            }

        }
    }
    private var mTimer : Timer? = null

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        App.application.registerBusListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.application.unregisterBusListener(this)
    }


    override fun onResume() {
        super.onResume()

        val currentDate = App.getCurrentDate()
        var time = currentDate.time

        if (App.application.storage.shouldRefresh(time)) {
            mHandler.obtainMessage(1).sendToTarget()
        }

        time %= Constants.TIMER_INTERVAL


        mTimer = Timer()
        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mHandler.obtainMessage(1).sendToTarget()
            }
        }, time, Constants.TIMER_INTERVAL)
    }

    override fun onPause() {
        super.onPause()
        mTimer!!.cancel()

    }

    @Subscribe
    fun handleUpdateListContentsEvent(event : UpdateListContentsEvent) {
        refreshContents()
    }


    override fun onCreateView(inflater : LayoutInflater?, container : ViewGroup?, savedInstanceState : Bundle?) : View? {
        val rootView = inflater!!.inflate(contentView, container, false) as ViewGroup

        val layout = LinearLayoutManager(context)
        rootView.list!!.layoutManager = layout

        rootView.swipe_refresh!!.setOnRefreshListener(this)
        rootView.swipe_refresh!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        adapter = ScheduleItemAdapter(this, layout, rootView.list)




        rootView.list.addOnScrollListener(object : ScheduleInfiniteScrollListener(layout) {
            override fun onLoadMore(page : Int, totalItemsCount : Int, view : RecyclerView) {
                adapter.load(page)
            }

        })
        rootView.list.adapter = adapter


        return rootView
    }

    override fun onViewCreated(view : View?, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshContents()
    }

    private fun hasScheduleItems() : Boolean {
        return !adapter.collection.isEmpty()
    }

    override fun hideViews() {
        empty.visibility = View.GONE
    }

    override fun showEmptyView() {
        empty.visibility = View.VISIBLE
    }

    override fun showErrorView() {
        empty.visibility = View.VISIBLE
    }

    private fun refreshContents() {
        val size = adapter.collection.size
        adapter.clear()
        adapter.notifyItemRangeRemoved(0, size)

        adapter.initContents()
    }

    private val contentView : Int
        get() = R.layout.fragment_schedule

    fun showFilters() {


        // previously invisible view




//        val filter = App.application.storage.filter
//
//        val types = Array(18, {Type(0, "Type")}).toList()
//
////        App.application.database.apply {
////            typeDao().getTypes()
////                    .subscribeOn(Schedulers.io())
////                    .observeOn(AndroidSchedulers.mainThread())
////                    .subscribe { types ->
//                        Logger.e("Creating filter view.")
//                        val view = FilterView(context, types, filter)
//                        Logger.e("View created.")
//                        MaterialAlert.create(context)
//                                .setTitle(getString(R.string.alert_filter_title))
//                                .setView(view)
//                                .setBasicNegativeButton()
//                                .setPositiveButton(R.string.save, DialogInterface.OnClickListener { _, _ ->
//                                    val filter = view.save()
//                                    App.application.analyticsController.tagFiltersEvent(filter)
//                                    refreshContents()
//                                }).show()
//                        Logger.e("Dialog created and shown.")
////                    }
////        }
//

    }


    override fun onRefresh() {
        val service = DatabaseService.create("DEFCON25")

        val syncRepository = SyncRepository(service)
        syncRepository.getSchedule()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onRefreshUpdate(it)
                }, {
                    onRefreshError(it)
                })

    }

    private fun onRefreshError(it : Throwable) {
        swipe_refresh?.isRefreshing = false
        if (context != null)
            Toast.makeText(context, context.getString(R.string.error_unable_to_sync), Toast.LENGTH_SHORT).show()

        Logger.e(it, "Could not refresh sync.")
    }

    private fun onRefreshUpdate(it : FullResponse) {
//        App.application.databaseController.createDatabase(response = it).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    swipe_refresh?.isRefreshing = false
//
//                    if (it == 0)
//                        Toast.makeText(context, context.getString(R.string.msg_up_to_date), Toast.LENGTH_SHORT).show()
//                    else if (it > 0) {
//                        App.application.postBusEvent(SyncResponseEvent(it))
//                        App.application.notificationHelper.scheduleUpdateNotification(it)
//                        refreshContents()
//                    }
//                }
    }


    companion object {

        fun newInstance() : ScheduleFragment {
            return ScheduleFragment()
        }
    }
}