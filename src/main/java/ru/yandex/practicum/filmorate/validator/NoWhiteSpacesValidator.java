package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.annotation.NoWhiteSpaces;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoWhiteSpacesValidator implements ConstraintValidator<NoWhiteSpaces, String> {

    @Override
    public void initialize(NoWhiteSpaces constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        String[] split = s.split(" ");
        return split.length == 1;
    }
}
