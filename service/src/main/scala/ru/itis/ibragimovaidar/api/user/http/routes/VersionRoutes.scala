package ru.itis.ibragimovaidar.api.user.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.circe._
import ru.itis.ibragimovaidar.api.user.services.VersionService

class VersionRoutes(versionService: VersionService) extends BaseRoutes {
  override val routes: HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "version" =>
        Ok(versionService.version)
    }
}
