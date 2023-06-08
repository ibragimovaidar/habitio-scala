package ru.itis.ibragimovaidar.api.user.services

import cats.effect.IO
import org.scalatest.EitherValues
import ru.itis.ibragimovaidar.api.user.SpecBase
import ru.itis.ibragimovaidar.api.user.data.UserDataGenerator
import ru.itis.ibragimovaidar.api.user.domain.User
import ru.itis.ibragimovaidar.api.user.errors.{UserNameAlreadyInUse, UserNotFound}
import ru.itis.ibragimovaidar.api.user.repository.UserRepository

import scala.util.Random

class UserServiceSpec extends SpecBase with EitherValues {

  private val userRepository = new UserRepository {
    override def insert(user: User): IO[Option[Int]] = IO.pure(Some(1))

    override def find(id: Int): IO[Option[User]] = IO.pure(Some(UserDataGenerator.generateUser))

    override def delete(id: Int): IO[Int] = IO.pure(1)

    override def update(id: Int, user: User): IO[Unit] = IO.pure(())

    override def findByEmail(email: String): IO[Option[User]] =
      IO.pure(Some(UserDataGenerator.generateUser))

    override def search(user: Int, search: String): IO[Seq[User]] = IO.pure(Seq(UserDataGenerator.generateUser))
  }

  private val errorUserRepository = new UserRepository {
    override def insert(user: User): IO[Option[Int]] = IO.pure(None)

    override def find(id: Int): IO[Option[User]] = IO.pure(None)

    override def delete(id: Int): IO[Int] = IO.pure(0)

    override def update(id: Int, user: User): IO[Unit] = IO.pure(())

    override def findByEmail(email: String): IO[Option[User]] = IO.pure(None)

    override def search(user: Int, search: String): IO[Seq[User]] = IO.pure(Seq.empty)
  }

  private val userService = new UserService(userRepository)
  private val errorUserService = new UserService(errorUserRepository)

  "UserService" when {
    "createUser() is called" should {
      "return the generated userId if the email is not in use" in {
        val result =
          userService
            .createUser(UserDataGenerator.generateCreateUserRequest)
            .unsafeRunSync
        result.right.value shouldBe 1
      }

      "return an error if the email is already in use" in {
        val result =
          errorUserService
            .createUser(UserDataGenerator.generateCreateUserRequest)
            .unsafeRunSync
        result.left.value shouldBe UserNameAlreadyInUse
      }

    }

    "findUser() is called" should {
      "return the user information for an existing id" in {
        val result =
          userService
            .findUser(999)
            .unsafeRunSync
        result.isDefined shouldBe true
      }

      "return None if the id is invalid" in {
        val result =
          errorUserService
            .findUser(9999)
            .unsafeRunSync
        result.isEmpty shouldBe true
      }
    }

    "deleteUser() is called" should {
      "return unit if the id exists" in {
        val result =
          userService
            .deleteUser(999)
            .unsafeRunSync
        result.isRight shouldBe true
      }
      "return UserNotFound if the id doesn't exist" in {
        val result =
          errorUserService
            .deleteUser(999)
            .unsafeRunSync
        result.left.value shouldBe UserNotFound
      }
    }

    "updateUser() is called" should {
      "return Unit if update go right" in {
        val result =
          userService
            .updateUser(999, UserDataGenerator.generateUpdateUserRequest)
            .unsafeRunSync
        result.isRight shouldBe true
      }

      "return UserNotFound if the id doesn't exist" in {
        val result =
          errorUserService
            .updateUser(999, UserDataGenerator.generateUpdateUserRequest)
            .unsafeRunSync
        result.left.value shouldBe UserNotFound
      }
    }

    "searchUsers() is called" should {
      "return the list of users that match the search query" in {
        userService
          .searchUsers(1, Random.nextString(5))
          .map(_.items.isEmpty shouldBe false)
          .unsafeToFuture
      }

      "return an empty result it there aren't users that match the query" in {
        errorUserService
          .searchUsers(1, Random.nextString(5))
          .map(_.items.isEmpty shouldBe true)
          .unsafeToFuture
      }

    }

  }

}
