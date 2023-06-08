package ru.itis.ibragimovaidar.api.user.validation.user

import cats.syntax.contravariantSemigroupal._
import ru.itis.ibragimovaidar.api.user.http.dto.ChangePasswordRequest

object ChangePasswordValidation extends UserValidator {

  def apply(req: ChangePasswordRequest): ValidationResult[ChangePasswordRequest] =
    (
      validatePassword(req.oldPassword),
      validatePassword(req.newPassword)
    ).mapN(ChangePasswordRequest.apply)

}
