package ru.itis.ibragimovaidar.api.user

import java.util.concurrent.Executors
import cats.effect.{ExitCode, IOApp}
import org.http4s.server.blaze.BlazeServerBuilder
import com.typesafe.scalalogging.LazyLogging
import cats.effect.IO
import org.http4s.server.AuthMiddleware
import ru.itis.ibragimovaidar.api.user.config.AppConfig
import ru.itis.ibragimovaidar.api.user.database.DatabaseManager
import ru.itis.ibragimovaidar.api.user.http.RoutesBuilder
import ru.itis.ibragimovaidar.api.user.http.authentication.{Authentication, UserInfo}
import ru.itis.ibragimovaidar.api.user.http.routes.{HealthRoutes, ProfileRoutes, SignUpRoutes, UserRoutes, VersionRoutes}
import ru.itis.ibragimovaidar.api.user.repository.UserRepository
import ru.itis.ibragimovaidar.api.user.services.{ProfileService, UserService, VersionService}

import scala.concurrent.ExecutionContext

object UserApiMain extends IOApp with LazyLogging {

  def run(args: List[String]): IO[ExitCode] = {

    logger.info("Starting service...")

    AppConfig.load flatMap { config =>
      val serviceConfig = config.service
      val postgresConfig = config.postgres

      implicit val executionContext: ExecutionContext =
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(serviceConfig.threads))

      DatabaseManager.generateTransactor(postgresConfig)(contextShift, executionContext).use { xa =>
        val userRepository = UserRepository(xa)

        val middleware: AuthMiddleware[IO, UserInfo] = Authentication.basic(userRepository)

        val userService = new UserService(userRepository)
        val versionService = new VersionService
        val profileService = new ProfileService(userRepository)

        val routes = Seq(
          new VersionRoutes(versionService),
          new SignUpRoutes(userService, serviceConfig),
          new UserRoutes(userService, middleware),
          new ProfileRoutes(profileService, middleware)
        )

        val routesBuilder = new RoutesBuilder(routes, new HealthRoutes, serviceConfig)
        val httpApp = routesBuilder.buildApp

        //migrate the database schema using flyway
        DatabaseManager.migrateSchema(xa).flatMap { _ =>
          BlazeServerBuilder[IO](executionContext)
            .withExecutionContext(executionContext)
            .bindHttp(serviceConfig.port, serviceConfig.host)
            .withHttpApp(httpApp)
            .resource
            .use(_ => IO.never)
            .as(ExitCode.Success)
        }

      }

    }
  }

}
