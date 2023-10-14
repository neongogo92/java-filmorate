package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.NoWhiteSpacesValidator;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoWhiteSpacesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoWhiteSpaces {
    String message() default "Не может быть пробелов";

    Class[] groups() default {};

    Class[] payload() default {};
}
