package ru.itis.ibragimovaidar.api.user.services

import java.sql.Timestamp
import java.time.LocalDateTime
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import ru.itis.ibragimovaidar.api.user.converters.UserConverters._
import ru.itis.ibragimovaidar.api.user.errors.{CreateUserError, GenericError, UserNameAlreadyInUse, UserNotFound}
import ru.itis.ibragimovaidar.api.user.http.dto.{CreateUserRequest, SearchUsersResponse, UpdateUserRequest, UserDetailResponse}
import ru.itis.ibragimovaidar.api.user.repository.UserRepository
import ru.itis.ibragimovaidar.api.user.util.StringUtils

class UserService(userRepository: UserRepository) extends LazyLogging {

  def createUser(createUserRequest: CreateUserRequest): IO[Either[CreateUserError, Int]] = {

    userRepository.insert(createUserRequest).map {
      case Some(id) =>
        logger.info(s"Generated a new user with id $id")
        Right(id)
      case None =>
        Left(UserNameAlreadyInUse)
    }

  }

  def findUser(id: Int): IO[Option[UserDetailResponse]] =
    userRepository.find(id).map { result => result.map(convertToUserDetail) }

  def deleteUser(id: Int): IO[Either[GenericError, Unit]] =
    userRepository.delete(id) map { value =>
      if (value == 0) {
        logger.info(s"Not found user with id $id")
        Left(UserNotFound)
      } else Right(())
    }

  def updateUser(id: Int, updateUserRequest: UpdateUserRequest): IO[Either[GenericError, Unit]] =
    userRepository.find(id).flatMap {
      case Some(user) =>
        val updatedUser = user
          .copy(
            firstName = updateUserRequest.firstname,
            lastName = updateUserRequest.lastname,
            gender = updateUserRequest.gender,
            dateOfBirth = StringUtils.getDateFromString(updateUserRequest.dateOfBirth),
            lastUpdatedDate = Some(Timestamp.valueOf(LocalDateTime.now()))
          )

        userRepository
          .update(id, updatedUser)
          .map(_ => Right())
      case None =>
        logger.info(s"Not found user with id $id")
        IO(Left(UserNotFound))
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

  def searchUsers(loggedUser: Int, query: String): IO[SearchUsersResponse] =
    userRepository
      .search(loggedUser, query)
      .map(result => SearchUsersResponse(result.map(convertToUserSearchDetail)))
}
