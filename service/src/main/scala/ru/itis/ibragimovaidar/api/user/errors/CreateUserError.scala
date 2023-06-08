package ru.itis.ibragimovaidar.api.user.errors

sealed trait CreateUserError

object UserNameAlreadyInUse extends CreateUserError
