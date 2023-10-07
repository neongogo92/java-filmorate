package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.FilmReleaseDateValidator;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilmReleaseDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReleaseDate {
    String message() default "Дата релиза не может быть больше 28 декабря 1895 года";

    Class[] groups() default {};

    Class[] payload() default {};
}
