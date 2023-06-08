package ru.itis.ibragimovaidar.api.user

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s.{AuthedRoutes, Request}
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder._
import ru.itis.ibragimovaidar.api.user.http.authentication.UserInfo
import ru.itis.ibragimovaidar.api.user.http.dto.ErrorsResponse

trait SpecBase extends AnyWordSpec with Matchers {
  protected val UserId = 1
  def mockAuthMiddleWare: AuthMiddleware[IO, UserInfo] = {
    val onFailure: AuthedRoutes[ErrorsResponse, IO] = Kleisli { req => OptionT.liftF(Forbidden(req.context)) }
    val authUser: Kleisli[IO, Request[IO], Either[ErrorsResponse, UserInfo]] = Kleisli({ _ =>
      IO.pure(Right(UserInfo(UserId, enabled = true)))
    })
    AuthMiddleware(authUser, onFailure)
  }
}
