package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Login cannot be blank")
    @Pattern(regexp = "^[^\\s]+$", message = "Login cannot contain spaces")
    private String login;

    private String name;

    @Past(message = "Birthday cannot be in the future")
    @NotNull(message = "Birthday cannot be null")
    private LocalDate birthday;
}
