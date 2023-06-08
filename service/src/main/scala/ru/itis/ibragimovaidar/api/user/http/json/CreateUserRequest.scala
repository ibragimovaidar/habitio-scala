package ru.itis.ibragimovaidar.api.user.http.json

case class CreateUserRequest(
    firstname: String,
    lastname: String,
    email: String,
    password: String,
    dateOfBirth: String,
    gender: String
)
