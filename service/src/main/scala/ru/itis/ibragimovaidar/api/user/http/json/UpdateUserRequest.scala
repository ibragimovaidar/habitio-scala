package ru.itis.ibragimovaidar.api.user.http.json

//not considering change email for semplicity
case class UpdateUserRequest(
    firstname: String,
    lastname: String,
    dateOfBirth: String,
    gender: String
)
