package ru.itis.ibragimovaidar.api.user.validation.user

import cats.syntax.contravariantSemigroupal._
import ru.itis.ibragimovaidar.api.user.http.dto.CreateUserRequest

object CreateUserValidation extends UserValidator {

  def apply(req: CreateUserRequest): ValidationResult[CreateUserRequest] =
    (
      validateFirstName(req.firstname),
      validateLastName(req.lastname),
      validateEmail(req.email),
      validatePassword(req.password),
      validateBirthDate(req.dateOfBirth),
      validateGender(req.gender)
    ).mapN(CreateUserRequest.apply)

}
