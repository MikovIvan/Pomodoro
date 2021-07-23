package ru.mikov.pomodoro.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.mikov.pomodoro.*
import ru.mikov.pomodoro.data.Timer
import ru.mikov.pomodoro.data.TimerStatus
import ru.mikov.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private val viewBinding: ActivityMainBinding by viewBinding()

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        with(viewBinding) {
            rvTimers.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = timerAdapter
            }

            btnAddTimer.setOnClickListener {
                if (etTime.text.isNotBlank()) {
                    timers.add(
                        Timer(
                            nextId++,
                            etTime.text.toString().toLong() * 1000L * 60L,
                            etTime.text.toString().toLong() * 1000L * 60L,
                            TimerStatus.STOP
                        )
                    )
                    timerAdapter.submitList(timers.toList())
                }
            }
        }

    }

    override fun start(id: Int) {
        changeTimer(id, null, TimerStatus.RUNNING)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, TimerStatus.STOP)
    }


    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    private fun changeTimer(id: Int, currentMs: Long?, status: TimerStatus) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            when {
                it.id == id -> {
                    newTimers.add(Timer(it.id, it.fullTime, currentMs ?: it.currentMs, status))
                }
                it.status == TimerStatus.RUNNING -> {
                    newTimers.add(Timer(it.id, it.fullTime, currentMs ?: it.currentMs, TimerStatus.STOP))
                }
                else -> newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (timerAdapter.currentList.find { it.status == TimerStatus.RUNNING } != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, timerAdapter.currentList.find { it.status == TimerStatus.RUNNING }!!.currentMs)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}