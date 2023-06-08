package ru.itis.ibragimovaidar.api.user.http.dto

case class CreateUserRequest(
    firstname: String,
    lastname: String,
    email: String,
    password: String,
    dateOfBirth: String,
    gender: String
)
