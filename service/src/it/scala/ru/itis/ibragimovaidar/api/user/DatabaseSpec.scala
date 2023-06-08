package ru.itis.ibragimovaidar.api.user

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import ru.itis.ibragimovaidar.api.user.config.PostgresConfig
import ru.itis.ibragimovaidar.api.user.database.DatabaseManager

import scala.concurrent.ExecutionContext.global
import scala.util.Random

trait DatabaseSpec extends AsyncWordSpecLike with Matchers {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  lazy val postgres: PostgreSQLContainer = PostgreSQLContainer(databaseName = s"user-${Random.nextInt(5)}")

  protected lazy val transactor: HikariTransactor[IO] = {
    val postgresConfig: PostgresConfig = PostgresConfig(
      user = postgres.username,
      password = postgres.password,
      jdbcUrl = postgres.jdbcUrl
    )
    DatabaseManager
      .generateTransactor(postgresConfig)(contextShift, global)
      .allocated
      .unsafeRunSync()._1
  }


}
