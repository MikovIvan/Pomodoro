package ru.mikov.pomodoro.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.mikov.pomodoro.R
import ru.mikov.pomodoro.data.Timer
import ru.mikov.pomodoro.data.TimerStatus
import ru.mikov.pomodoro.databinding.TimerItemBinding
import ru.mikov.pomodoro.extensions.displayTime
import ru.mikov.pomodoro.extensions.startAnimation
import ru.mikov.pomodoro.extensions.stopAnimation
import ru.mikov.pomodoro.service.UNIT_TEN_MS

class TimerAdapter(
    private val listener: TimerListener,
) : ListAdapter<Timer, TimerViewHolder>(TimerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerItemBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, listener, binding.root.context)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private var timers: MutableMap<Int, CountDownTimer> = mutableMapOf()
    private val objAnimator = ObjectAnimator.ofFloat(binding.ivIndicator, "alpha", 0f, 1f)

    fun bind(timer: Timer) {
        with(binding) {
            tvTimer.text = timer.currentMs.displayTime()

            if (!isRecyclable) setIsRecyclable(true)

            when (timer.status) {
                TimerStatus.RUNNING -> {
                    startTimer(timer)
                    timerView.setPeriod(timer.fullTime)
                    btnStart.text = context.getString(R.string.stop)
                    root.setCardBackgroundColor(context.getColor(R.color.white))
                    setIsRecyclable(false)
                }
                TimerStatus.STOP -> {
                    stopTimer(timer)
                    timerView.setPeriod(timer.fullTime)
                    timerView.setCurrent(timer.currentMs)
                    btnStart.text = context.getString(R.string.start)
                }
                TimerStatus.FINISH -> {
                    timerView.setCurrent(0)
                    root.setCardBackgroundColor(context.getColor(R.color.red_700))
                    setIsRecyclable(false)
                }
            }
        }
        initButtonsListeners(timer)
    }

    private fun stopTimer(timer: Timer) {
        with(binding) {
            timers.get(timer.id)?.cancel()
            ivIndicator.stopAnimation(objAnimator)
        }
    }

    private fun initButtonsListeners(timer: Timer) {
        with(binding) {
            btnStart.setOnClickListener {
                when (timer.status) {
                    TimerStatus.RUNNING -> listener.stop(timer.id, timer.currentMs)
                    TimerStatus.FINISH -> listener.start(timer.id)
                    else -> listener.start(timer.id)
                }
            }

            btnDeleteTimer.setOnClickListener {
                if (!isRecyclable) setIsRecyclable(true)
                timers.get(timer.id)?.cancel()
                timers.remove(timer.id)
                listener.delete(timer.id)
                root.setCardBackgroundColor(context.getColor(R.color.white))
            }
        }
    }

    private fun startTimer(timer: Timer) {
        with(binding) {
            ivIndicator.startAnimation(objAnimator)
            root.setCardBackgroundColor(context.getColor(R.color.white))
            timers.get(timer.id)?.cancel()
            timers.put(timer.id, getCountDownTimer(timer))
            timers.get(timer.id)?.start()
        }
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.currentMs, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                with(binding) {
                    tvTimer.text = millisUntilFinished.displayTime()
                    timer.currentMs = millisUntilFinished
                    timerView.setCurrent(millisUntilFinished)
                }

            }

            override fun onFinish() {
                with(binding) {
                    tvTimer.text = timer.fullTime.displayTime()
                    timerView.setCurrent(0)
                    ivIndicator.stopAnimation(objAnimator)
                    root.setCardBackgroundColor(context.getColor(R.color.red_700))
                    timer.status = TimerStatus.FINISH
                    timer.currentMs = timer.fullTime
                    btnStart.text = context.getString(R.string.start)
                }
            }
        }
    }
}

class TimerDiffCallback : DiffUtil.ItemCallback<Timer>() {
    override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean =
        oldItem.currentMs == newItem.currentMs && oldItem.status == newItem.status
                && oldItem.fullTime == newItem.fullTime

    override fun getChangePayload(oldItem: Timer, newItem: Timer) = Any()
}