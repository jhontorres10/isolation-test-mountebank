package com.example.mountebank.model;

import java.net.URL;
import java.time.LocalDate;

public record Book(int id,
                   String title,
                   String author,
                   String genre,
                   String description,
                   String isbn,
                   URL image,
                   LocalDate published,
                   String publisher) {
}
