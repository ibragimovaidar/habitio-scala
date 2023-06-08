package ru.itis.ibragimovaidar.api.user.errors

sealed trait GenericError

case object UserNotFound extends GenericError
