package ru.itis.ibragimovaidar.api.user.http.authentication

import cats.effect.IO
import org.http4s.{AuthedRoutes, BasicCredentials, Header, Headers, Request}
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import ru.itis.ibragimovaidar.api.user.SpecBase
import ru.itis.ibragimovaidar.api.user.data.UserDataGenerator
import ru.itis.ibragimovaidar.api.user.domain.User
import ru.itis.ibragimovaidar.api.user.repository.UserRepository

import scala.util.Random

class BasicAuthenticationSpec extends SpecBase {

  private val user = UserDataGenerator.generateUser

  private val userRepository = new UserRepository {
    override def insert(user: User): IO[Option[Int]] = IO.pure(None)
    override def find(id: Int): IO[Option[User]] = IO.pure(None)
    override def findByEmail(email: String): IO[Option[User]] =
      IO.pure(Some(user.copy(email = email)))
    override def update(id: Int, user: User): IO[Unit] = IO.pure(())
    override def delete(id: Int): IO[Int] = IO.pure(1)
    override def search(user: Int, search: String): IO[Seq[User]] = IO.pure(Seq.empty)
  }

  val authentication = Authentication.basic(userRepository)

  private val simpleTestRoutes: AuthedRoutes[UserInfo, IO] = AuthedRoutes.of {
    case GET -> Root / "test" as user =>
      NoContent()
  }

  private val authTestRoutes = authentication(simpleTestRoutes).orNotFound

  "BasicAuthentication" should {
    "return Forbidden when the Authorization header is not provided" in {

      val result = authTestRoutes.run(Request(uri = uri"/test")).unsafeRunSync
      result.status shouldBe Forbidden
    }

    "return Forbidden when the credentials are incorrect" in {
      val credentials =
        BasicCredentials(Random.nextString(10), user.password + Random.nextString(5)).token
      val headers = Headers.of(Header.apply(Authorization.name.toString, s"Basic: $credentials"))

      val result = authTestRoutes.run(Request(uri = uri"/test").withHeaders(headers)).unsafeRunSync
      result.status shouldBe Forbidden
    }

  }

}
