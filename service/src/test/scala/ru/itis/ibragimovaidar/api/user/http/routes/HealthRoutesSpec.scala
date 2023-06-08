package ru.itis.ibragimovaidar.api.user.http.routes

import org.http4s.{Request, Status}
import org.http4s.implicits._
import ru.itis.ibragimovaidar.api.user.SpecBase

class HealthRoutesSpec extends SpecBase {

  private val healthRoutes = (new HealthRoutes).routes.orNotFound

  "HealthRoutes" should {
    "return NoContent when health endpoint is called" in {
      val response = healthRoutes.run(Request(uri = uri"/health")).unsafeRunSync()

      response.status shouldBe Status.NoContent

    }
  }

}
