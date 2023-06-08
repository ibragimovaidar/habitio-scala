package ru.itis.ibragimovaidar.api.user.validation.user

import cats.syntax.contravariantSemigroupal._
import ru.itis.ibragimovaidar.api.user.http.dto.UpdateUserRequest

object UpdateUserValidation extends UserValidator {

  def apply(req: UpdateUserRequest): ValidationResult[UpdateUserRequest] =
    (
      validateFirstName(req.firstname),
      validateLastName(req.lastname),
      validateBirthDate(req.dateOfBirth),
      validateGender(req.gender)
    ).mapN(UpdateUserRequest.apply)

}
