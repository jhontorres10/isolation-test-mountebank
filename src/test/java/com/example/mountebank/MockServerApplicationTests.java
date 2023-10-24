package com.example.mountebank;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MockServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Container
    static MockServerContainer mockServerContainer =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"));

    static MockServerClient mockServerClient;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        mockServerClient = new MockServerClient(
                mockServerContainer.getHost(),
                mockServerContainer.getServerPort()
        );
        registry.add("faker.api", mockServerContainer::getEndpoint);
    }

    @BeforeEach
    void setUp() {
        mockServerClient.reset();
    }

    @Test
    void contextLoadsHappyPath() throws Exception {
        var url = "/api/v1/books";
        Path fileName
                = Path.of("src/test/resources/__files/happyPath.json");
        String body = Files.readString(fileName);

        // set behavior
        mockServerClient
                .when(request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/api/v1/books")
                        .withQueryStringParameter("_quantity", "5")
                        .withQueryStringParameter("_locale", "en_US"))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(body));

        var expectedSize = 5;
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(expectedSize))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*]['title']").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*]['id']").exists());
    }

    @Test
    void contextLoadsUnhappyPath() {
        var url = "/api/v1/books";
        // set behavior
        mockServerClient
                .when(request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/api/v1/books")
                        .withQueryStringParameter("_quantity", "5")
                        .withQueryStringParameter("_locale", "en_US"))
                .respond(response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()));

        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }

    @Test
    void contextLoadsErrorTimeOut() {
        var url = "/api/v1/books";
        // set behavior
        mockServerClient
                .when(request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/api/v1/books")
                        .withQueryStringParameter("_quantity", "5")
                        .withQueryStringParameter("_locale", "en_US"))
                .respond(response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()));


        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }


}

