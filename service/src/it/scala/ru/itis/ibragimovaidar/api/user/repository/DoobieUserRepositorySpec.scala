package ru.itis.ibragimovaidar.api.user.repository

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Date
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import org.scalatest.{FutureOutcome, OptionValues}
import ru.itis.ibragimovaidar.api.user.DatabaseSpec
import ru.itis.ibragimovaidar.api.user.database.DatabaseManager
import ru.itis.ibragimovaidar.api.user.domain.User

import scala.util.Random

class DoobieUserRepositorySpec extends DatabaseSpec with ForAllTestContainer with OptionValues {

  private def generateUser = User(
    id = None,
    firstName = Random.nextString(5),
    lastName = Random.nextString(5),
    email = s"${Random.nextString(5)}@test.com",
    password = Random.nextString(5),
    dateOfBirth = new Date(1990, 6, 8),
    gender = "MALE",
    creationDate = Timestamp.valueOf(LocalDateTime.now()),
    enabled = false
  )

  override lazy val container: PostgreSQLContainer = postgres

  override def afterStart(): Unit = {
    DatabaseManager.migrateSchema(transactor).unsafeRunSync()//create the table schema
  }

  "DoobieUserRepository" when {

    "insert(user) is called" should {
      "return the id of the inserted user if it's a new user" in {
        val userRepository = createRepository

        userRepository
          .insert(generateUser)
          .map(value => value.isDefined shouldBe true)
          .unsafeRunSync()

      }

      "return None if the email is already in use" in {
        val userRepository = createRepository
        val user = generateUser

        userRepository.insert(user).unsafeRunSync() // insert the first user
        userRepository.insert(generateUser.copy(email = user.email)).map(_.isEmpty shouldBe true).unsafeToFuture()
      }
    }

    "find(id) is called" should {
      "return None if the id doesn't exist" in {
        val userRepository = createRepository
        createRepository
          .find(9999999).map(_.isEmpty shouldBe true)
          .unsafeToFuture()

      }

      "return the user if the id exist" in {
        val userRepository = createRepository
        val userToInsert = generateUser
        val id = createRepository.insert(userToInsert).unsafeRunSync().get//get the id generated

        userRepository.find(id).map{ result =>
          val user = result.value
          user.id.isDefined shouldBe true
          user.firstName shouldBe userToInsert.firstName
          user.lastName shouldBe userToInsert.lastName
          user.email shouldBe userToInsert.email
          user.password shouldBe userToInsert.password
          user.dateOfBirth shouldBe userToInsert.dateOfBirth
          user.gender shouldBe userToInsert.gender
          user.creationDate shouldBe userToInsert.creationDate
          user.enabled shouldBe userToInsert.enabled

        }.unsafeToFuture

      }

    }

    "findByEmail(email) is called" should {
      "return None if an user with the email doesn't exist" in {
        val userRepository = createRepository
        createRepository.findByEmail("test@test.com").map(_.isEmpty shouldBe true).unsafeToFuture
      }

      "return the user if an user with email exist" in {
        val userRepository = createRepository
        val userToInsert = generateUser
        createRepository.insert(userToInsert).unsafeRunSync()

        createRepository.findByEmail(userToInsert.email).map{ result =>

          val user = result.value
          user.email shouldBe userToInsert.email
        }.unsafeToFuture

      }
    }

    "delete(id) is called" should {
      "return 1 if the id exist" in {
        val userRepository = createRepository
        val userToInsert = generateUser
        val id = createRepository.insert(userToInsert).unsafeRunSync().get//get the id generated

        userRepository.delete(id).map(result => result shouldBe 1).unsafeToFuture
      }
      "return 0 if the id doesn't exist" in {
        val userRepository = createRepository
        userRepository.delete(999).map(result => result shouldBe 0).unsafeToFuture

      }
    }

    "update(id, user) is called" should {
      "return Unit when updating the user" in {
        val userRepository = createRepository
        val userToInsert = generateUser
        val userToUpdate = generateUser.copy(firstName = Random.nextString(5), lastName = Random.nextString(5))
        val id = createRepository.insert(userToInsert).unsafeRunSync().get//get the id generated

        userRepository.update(id, userToUpdate).unsafeRunSync //do the update object

        userRepository.find(id).map { result =>

          val user = result.value
          user.firstName shouldBe user.firstName
          user.lastName shouldBe user.lastName

        }.unsafeToFuture
      }
    }

  }

  private def createRepository: UserRepository = UserRepository(transactor)

}