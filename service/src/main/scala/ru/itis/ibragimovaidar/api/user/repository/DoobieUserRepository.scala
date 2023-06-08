package ru.itis.ibragimovaidar.api.user.repository

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import ru.itis.ibragimovaidar.api.user.domain.User

final class DoobieUserRepository(xa: Transactor[IO]) extends UserRepository {

  override def insert(user: User): IO[Option[Int]] = {
    val insertQuery =
      sql"""
           INSERT INTO users(firstname, lastname, email, password, birthday_date, gender, creation_date, enabled)
           VALUES(${user.firstName}, ${user.lastName}, ${user.email}, ${user.password}, ${user.dateOfBirth}, ${user.gender}, ${user.creationDate}, ${user.enabled})
           """.update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(xa)

    val checkIfExist =
      sql"""SELECT id, firstname, lastname, email, password, birthday_date, gender, creation_date, enabled, last_updated_date
            FROM users u
            WHERE u.email = ${user.email}
         """.stripMargin
        .query[User]
        .option
        .transact(xa)

    for {
      exist <- checkIfExist
      result <- exist match {
        case Some(_) => IO { None }
        case None    => insertQuery.map(Some(_))
      }
    } yield result

  }

  override def find(id: Int): IO[Option[User]] = {
    sql"""
          SELECT id, firstname, lastname, email, password, birthday_date, gender, creation_date, enabled, last_updated_date
          FROM users u
          WHERE u.id = $id
       """
      .query[User]
      .option
      .transact(xa)
  }

  override def delete(id: Int): IO[Int] =
    sql"""
         DELETE FROM users
         WHERE id = $id
        """.stripMargin.update.run
      .transact(xa)

  override def update(id: Int, user: User): IO[Unit] =
    sql"""
         UPDATE users
         SET firstname = ${user.firstName},
             lastname = ${user.lastName},
             gender = ${user.gender},
             birthday_date = ${user.dateOfBirth},
             enabled = ${user.enabled}
         WHERE id = $id
        """.stripMargin.update.run
      .transact(xa)
      .map(_ => ())

  override def findByEmail(email: String): IO[Option[User]] =
    sql"""
          SELECT id, firstname, lastname, email, password, birthday_date, gender, creation_date, enabled, last_updated_date
          FROM users u
          WHERE u.email = $email
       """
      .query[User]
      .option
      .transact(xa)

  override def search(user: Int, search: String): IO[Seq[User]] =
    sql"""
         SELECT id, firstname, lastname, email, password, birthday_date, gender, creation_date, enabled, last_updated_date
         FROM users u
         WHERE u.enabled = TRUE AND u.id <> $user AND
               (u.firstname LIKE '%' || $search || '%' OR u.lastname LIKE '%' ||$search || '%' OR u.email LIKE '%' ||$search || '%')
       """.stripMargin
      .query[User]
      .to[Seq]
      .transact(xa)

}
