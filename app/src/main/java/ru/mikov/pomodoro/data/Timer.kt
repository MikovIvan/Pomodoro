package ru.mikov.pomodoro.data

data class Timer(
    val id: Int,
    var fullTime: Long,
    var currentMs: Long,
    var status: TimerStatus
)

enum class TimerStatus {
    RUNNING, STOP, FINISH
}
