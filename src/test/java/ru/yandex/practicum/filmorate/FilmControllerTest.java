package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void addFilm_EmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        this.mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFilm_InvalidReleaseDate_ShouldReturnBadRequest() throws Exception {
        String invalidFilm = "{" +
                "\"name\": \"Test Film\", " +
                "\"description\": \"Test description\", " +
                "\"releaseDate\": \"2222-02-02\", " +
                "\"duration\": \"120\"" +
                "}";
        this.mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(invalidFilm))
                .andExpect(status().isBadRequest());
    }

}

