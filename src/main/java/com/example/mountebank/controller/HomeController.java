package com.example.mountebank.controller;

import com.example.mountebank.model.Book;
import com.example.mountebank.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeController {
    private final BookService bookService;

    @GetMapping("books")
    public List<Book> home() {
        return bookService.getBooks();
    }
}
