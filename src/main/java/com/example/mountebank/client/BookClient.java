package com.example.mountebank.client;

import com.example.mountebank.model.BookInformation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "books", url = "${faker.api}")
public interface BookClient {
    @GetMapping(value = "/api/v1/books")
    BookInformation getBooks(@RequestParam("_quantity") int quantity,
                             @RequestParam("_locale") String locale);
}