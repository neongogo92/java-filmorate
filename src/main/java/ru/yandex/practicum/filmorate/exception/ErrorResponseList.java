package ru.yandex.practicum.filmorate.exception;//package ru.yandex.practicum.filmorate.exception1;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorResponseList {
    private final List<ErrorResponse> errorResponses = new ArrayList<>();
}
