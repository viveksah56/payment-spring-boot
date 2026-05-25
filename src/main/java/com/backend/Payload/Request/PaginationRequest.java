package com.backend.Payload.Request;

public record PaginationRequest(
        int page,
        int size,
        String search,
        String sort,
        String sortDirection
) {
    private static final int DEFAULT_PAGE           = 1;
    private static final int DEFAULT_SIZE           = 10;
    private static final int MAX_SIZE               = 100;
    private static final String DEFAULT_SORT        = "createdAt";
    private static final String DEFAULT_SORT_DIR    = "desc";

    public int resolvedPage() {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    public int resolvedSize() {
        if (size <= 0) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    public String resolvedSearch() {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    public String resolvedSort() {
        return (sort == null || sort.isBlank()) ? DEFAULT_SORT : sort.trim();
    }

    public String resolvedSortDirection() {
        if (sortDirection == null || sortDirection.isBlank()) return DEFAULT_SORT_DIR;
        return sortDirection.trim().equalsIgnoreCase("asc") ? "asc" : "desc";
    }
}