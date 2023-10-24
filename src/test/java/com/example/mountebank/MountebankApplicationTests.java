package com.example.mountebank;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbtest.javabank.Client;
import org.mbtest.javabank.fluent.ImposterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static feign.Request.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MountebankApplicationTests {

    protected static final int DEFAULT_PORT = 8090;
    protected static final Client CLIENT = new Client();

    @Container
    public static GenericContainer<?> testcontainersBuilder;

    static {
        var testcontainersBuilderCopy = new GenericContainer<>("bbyars/mountebank:latest");
        testcontainersBuilderCopy.setPortBindings(List.of("2525:2525", "8090:8090"));
        testcontainersBuilder = testcontainersBuilderCopy;
    }

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        CLIENT.deleteAllImposters();
    }

    @Test
    void contextLoadsHappyPath() throws Exception {
        var url = "/api/v1/books";
        Path fileName
                = Path.of("src/test/resources/__files/happyPath.json");

        String body = Files.readString(fileName);

        var expectedImposter = ImposterBuilder.anImposter()
                .onPort(DEFAULT_PORT)
                .stub()
                .predicate()
                .equals()
                .method("GET")
                .path(url)
                .query("_quantity", "5")
                .query("_locale", "en_US")
                .end()
                .end()
                .response()
                .is()
                .statusCode(HttpStatus.OK.value())
                .header("Content-Type", "application/json")
                .body(body)
                .end()
                .end()
                .end()
                .build();


        CLIENT.createImposter(expectedImposter);

        var expectedSize = 5;
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andDo(print())
                .andExpect(status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(expectedSize))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[*]['title']").isNotEmpty())
//                .andExpect(MockMvcResultMatchers.jsonPath("$[*]['id']").exists())
        ;

    }

    @Test
    void contextLoadsUnhappyPath() {
        var url = "/api/v1/books";

        var expectedImposter = ImposterBuilder.anImposter()
                .onPort(DEFAULT_PORT)
                .stub()
                .predicate()
                .equals()
                .method("GET")
                .path(url)
                .query("_quantity", "5")
                .query("_locale", "en_US")
                .end()
                .end()
                .response()
                .is()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .end()
                .end()
                .end()
                .build();


        CLIENT.createImposter(expectedImposter);

        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }

    @Test
    void contextLoadsErrorTimeOut() {
        var url = "/api/v1/books";

        var expectedImposter = ImposterBuilder.anImposter()
                .onPort(DEFAULT_PORT)
                .stub()
                .predicate()
                .equals()
                .method(GET.name())
                .path(url)
                .query("_quantity", "5")
                .query("_locale", "en_US")
                .end()
                .end()
                .response()
                .is()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .end()
                .end()
                .end()
                .build();


        CLIENT.createImposter(expectedImposter);

        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }


}

