package ru.itis.ibragimovaidar.api.user.errors

sealed trait ChangePasswordError

object WrongOldPasswordError extends ChangePasswordError
