package com.backend.Controller;

import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.ApiResponse;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PaginationRequest request = new PaginationRequest(page, size, search, sort, sortDirection);
        PaginationResponse<ProductResponse> products = productService.getProducts(request);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID productId) {
        ProductResponse product = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}