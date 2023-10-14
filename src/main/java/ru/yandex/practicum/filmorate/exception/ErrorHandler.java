package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
class ErrorHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ErrorResponseList getConstraintValidationException(ConstraintViolationException e) {
    ErrorResponseList error = new ErrorResponseList();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      error
          .getErrorResponses()
          .add(new ErrorResponse(violation.getPropertyPath().toString(), violation.getMessage()));

      log.error(
          "ConstraintValidation error: {} - {}",
          violation.getPropertyPath().toString(),
          violation.getMessage());
    }
    return error;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ErrorResponseList getMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    ErrorResponseList error = new ErrorResponseList();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      error
          .getErrorResponses()
          .add(new ErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()));

      log.error(
          "MethodArgumentNotValid error: {} - {}",
          fieldError.getField(),
          fieldError.getDefaultMessage());
    }
    return error;
  }

  @ExceptionHandler({
    CustomExceptions.UserDoesNotExistsException.class,
    CustomExceptions.FilmDoesNotExistsException.class,
    CustomExceptions.MpaDoesNotExistsException.class,
    CustomExceptions.GenreDoesNotExistsException.class
  })
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  ErrorResponse getModelException(RuntimeException e) {
    log.error("Объект не найден: {}", e.getMessage());
    return new ErrorResponse("Объект не найден", e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  ErrorResponse getInternalException(Exception e) {
    log.error("Внутренняя ошибка: {}", e.getMessage());
    return new ErrorResponse("Внутренняя ошибка", e.getMessage());
  }
}
