package com.backend.Payload.Respone;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginationResponse<T>(
        List<T> data,
        int page,
        int size,
        long total,
        int totalPages,
        String sortBy,
        String sortDir
) {
    public PaginationResponse {
        if (page < 1) throw new IllegalArgumentException("Page index must be >= 1");
        if (size <= 0) throw new IllegalArgumentException("Page size must be > 0");
        if (total < 0) throw new IllegalArgumentException("Total count must be >= 0");
    }

    public static <T> PaginationResponse<T> of(Page<T> page) {
        String sortBy = page.getSort().isSorted()
                ? page.getSort().iterator().next().getProperty()
                : "createdAt";

        String sortDir = page.getSort().isSorted()
                ? page.getSort().iterator().next().getDirection().name().toLowerCase()
                : "desc";

        return new PaginationResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortBy,
                sortDir
        );
    }
}