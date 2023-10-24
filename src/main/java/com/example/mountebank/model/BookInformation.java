package com.example.mountebank.model;

import java.util.List;

public record BookInformation(String status,
                              int code,
                              int total,
                              List<Book> data
) {
}
