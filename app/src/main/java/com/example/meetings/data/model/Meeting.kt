package com.example.meetings.data.model

data class Meeting(
    val uuid: String,
    val name: String,
    val authorId: Int,
    val author: String,
    val createdAt: String,
    val duration: Int,
    val comment: String?,
    val link: String?,
    val status: String,
    val users: List<User>
)

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val patronymic: String
)