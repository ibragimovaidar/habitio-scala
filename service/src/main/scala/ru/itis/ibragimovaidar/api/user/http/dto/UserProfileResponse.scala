package ru.itis.ibragimovaidar.api.user.http.dto

case class UserProfileResponse(
    id: Int,
    firstname: String,
    lastname: String,
    email: String,
    dateOfBirth: String,
    gender: String,
    creationDate: String,
    enabled: Boolean
)
