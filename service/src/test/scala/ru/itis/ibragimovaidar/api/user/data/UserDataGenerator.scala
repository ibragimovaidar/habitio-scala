package ru.itis.ibragimovaidar.api.user.data

import java.sql.Timestamp
import ru.itis.ibragimovaidar.api.user.domain.User
import ru.itis.ibragimovaidar.api.user.http.json.{CreateUserRequest, SearchUserItem, UpdateUserRequest, UserDetailResponse, UserProfileResponse}
import ru.itis.ibragimovaidar.api.user.util.StringUtils

import scala.util.Random

object UserDataGenerator {

  def generateCreateUserRequest: CreateUserRequest =
    CreateUserRequest(
      firstname = Random.nextString(5),
      lastname = Random.nextString(5),
      email = "test@mail.com",
      password = Random.nextString(9),
      dateOfBirth = "07-09-1990",
      gender = "MALE"
    )

  def generateUser: User =
    User(
      firstName = Random.nextString(5),
      lastName = Random.nextString(5),
      email = Random.nextString(5),
      password = s"password-${Random.nextInt(6)}",
      dateOfBirth = StringUtils.getDateFromString("07-07-2007"),
      gender = "MALE",
      creationDate = new Timestamp(System.currentTimeMillis()),
      enabled = true
    )

  def generateUserProfileResponse: UserProfileResponse =
    UserProfileResponse(
      id = Random.nextInt(5),
      firstname = Random.nextString(5),
      lastname = Random.nextString(5),
      email = Random.nextString(5),
      dateOfBirth = "07-07-2000",
      gender = "MALE",
      creationDate = "09-09-2012",
      enabled = true
    )

  def generateUserDetailResponse: UserDetailResponse =
    UserDetailResponse(
      firstname = Random.nextString(5),
      lastname = Random.nextString(5),
      email = Random.nextString(5),
      dateOfBirth = "07-07-2000",
      gender = "MALE",
      creationDate = "09-09-2012"
    )

  def generateUserSearchResponse: SearchUserItem =
    SearchUserItem(
      id = Random.nextInt(5),
      firstName = Random.nextString(5),
      lastName = Random.nextString(5),
      email = Random.nextString(5),
      dateOfBirth = "07-07-2000",
      gender = "MALE",
      creationDate = "09-09-2012"
    )

  def generateUpdateUserRequest: UpdateUserRequest =
    UpdateUserRequest(
      firstname = Random.nextString(5),
      lastname = Random.nextString(5),
      dateOfBirth = "07-09-1990",
      gender = "MALE"
    )

}
