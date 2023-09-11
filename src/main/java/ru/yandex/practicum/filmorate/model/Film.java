package ru.yandex.practicum.filmorate.model;


import lombok.Data;
import ru.yandex.practicum.filmorate.exception.ValidReleaseDate;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {

    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @PastOrPresent(message = "Release date cannot be in the future")
    @NotNull(message = "Release date cannot be null")
    @ValidReleaseDate(message = "Release date before 1895")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be positive")
    private Integer duration;
}
