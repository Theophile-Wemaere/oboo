package fr.isep.oboo.dto

data class TimeSlotDTO(
    val id: Int,
    val subject: String,
    val start_time: String,
    val end_time: String,
    val room: Int
)
