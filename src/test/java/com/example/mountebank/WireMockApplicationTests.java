package com.example.mountebank;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8090) // imposter
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WireMockApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoadsHappyPath(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        var url = "/api/v1/books?_quantity=5&_locale=en_US";
        var url_addresses = "/api/v1/addresses?_quantity=5&_locale=en_US";
        // set behavior
        stubFor(get(url)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("happyPath.json")));

        var expectedSize = 5;
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andDo(print())
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(expectedSize))
//                .andExpect(jsonPath("$[*]['title']").isNotEmpty())
//                .andExpect(jsonPath("$[*]['id']").exists())
        ;

        verify(1, getRequestedFor(urlEqualTo(url)));
        verify(0, getRequestedFor(urlEqualTo(url_addresses)));
    }

    @Test
    void contextLoadsUnhappyPath(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        // set behavior
        stubFor(get("/api/v1/books?_quantity=5&_locale=en_US")
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }

    @Test
    void contextLoadsErrorTimeOut(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        // set behavior
        var duration = Duration.ofSeconds(3);
        stubFor(get("/api/v1/books?_quantity=5&_locale=en_US").willReturn(
                aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withFixedDelay((int) duration.toMillis())));

        assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get("/books")));
    }


}

