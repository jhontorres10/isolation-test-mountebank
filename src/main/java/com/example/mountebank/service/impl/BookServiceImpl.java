package com.example.mountebank.service.impl;

import com.example.mountebank.client.BookClient;
import com.example.mountebank.model.Book;
import com.example.mountebank.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookClient bookClient;

    @Override
    public List<Book> getBooks() {
        return bookClient.getBooks(5, "en_US").data();
    }
}
