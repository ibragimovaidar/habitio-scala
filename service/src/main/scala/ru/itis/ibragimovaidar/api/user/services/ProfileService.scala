package ru.itis.ibragimovaidar.api.user.services

import java.sql.Timestamp
import java.time.LocalDateTime
import cats.effect.IO
import ru.itis.ibragimovaidar.api.user.converters.UserConverters._
import ru.itis.ibragimovaidar.api.user.errors.{ChangePasswordError, WrongOldPasswordError}
import ru.itis.ibragimovaidar.api.user.http.json.UserProfileResponse
import ru.itis.ibragimovaidar.api.user.repository.UserRepository

class ProfileService(userRepository: UserRepository) {

  def getProfile(id: Int): IO[UserProfileResponse] =
    userRepository.find(id).map { result =>
      val user = result.get
      convertToUserProfile(user)
    }

  def activateUser(id: Int): IO[Unit] =
    userRepository.find(id) flatMap {
      case Some(user) =>
        val updatedUser = user.copy(
          enabled = true,
          lastUpdatedDate = Some(Timestamp.valueOf(LocalDateTime.now()))
        )
        userRepository
          .update(id, updatedUser)
          .map(_ => Right())
    }

  def changePassword(userId: Int, oldPassword: String, newPassword: String): IO[Either[ChangePasswordError, Unit]] = {

    userRepository.find(userId) flatMap {
      case Some(user) =>
        if (user.password != oldPassword) IO(Left(WrongOldPasswordError))
        else {
          val newUser = user.copy(password = newPassword)
          userRepository
            .update(userId, newUser)
            .map(_ => Right())
        }
    }
  }

}
