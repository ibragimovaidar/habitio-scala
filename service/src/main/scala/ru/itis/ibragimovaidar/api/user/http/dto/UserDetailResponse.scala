package ru.itis.ibragimovaidar.api.user.http.dto

case class UserDetailResponse(
    firstname: String,
    lastname: String,
    email: String,
    dateOfBirth: String,
    gender: String,
    creationDate: String
)
