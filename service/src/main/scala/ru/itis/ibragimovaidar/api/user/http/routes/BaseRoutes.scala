package ru.itis.ibragimovaidar.api.user.http.routes

import cats.data.NonEmptyChain
import cats.effect.IO
import org.http4s.HttpRoutes
import ru.itis.ibragimovaidar.api.user.http.json.{ErrorItem, ErrorsResponse}
import ru.itis.ibragimovaidar.api.user.validation.DomainValidation

trait BaseRoutes {

  def routes: HttpRoutes[IO]

  def getErrorsResponse(items: NonEmptyChain[DomainValidation]): ErrorsResponse = {
    val list =
      items.toNonEmptyList.toList.map(item => ErrorItem(Some(item.field), item.errorMessage))
    ErrorsResponse(list)
  }

}
