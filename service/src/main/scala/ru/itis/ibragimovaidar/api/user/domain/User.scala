package ru.itis.ibragimovaidar.api.user.domain

import java.sql.Timestamp
import java.util.Date

final case class User(
    id: Option[Int] = None,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    dateOfBirth: Date,
    gender: String,
    creationDate: Timestamp,
    enabled: Boolean,
    lastUpdatedDate: Option[Timestamp] = None
)
