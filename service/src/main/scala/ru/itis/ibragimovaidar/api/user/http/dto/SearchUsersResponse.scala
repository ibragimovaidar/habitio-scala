package ru.itis.ibragimovaidar.api.user.http.dto

case class SearchUserItem(
    id: Int,
    firstName: String,
    lastName: String,
    email: String,
    dateOfBirth: String,
    gender: String,
    creationDate: String
)

case class SearchUsersResponse(items: Seq[SearchUserItem])
