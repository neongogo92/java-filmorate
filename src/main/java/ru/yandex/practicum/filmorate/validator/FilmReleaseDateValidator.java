package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilmReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, LocalDate> {
    @Override
    public void initialize(ValidReleaseDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return releaseDate.isAfter(LocalDate.parse("1895-12-28", formatter));
    }
}
