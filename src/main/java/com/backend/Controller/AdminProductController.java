package com.backend.Controller;

import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Request.ProductRequest.CreateProductRequest;
import com.backend.Payload.Request.ProductRequest.RestockRequest;
import com.backend.Payload.Request.ProductRequest.UpdateProductRequest;
import com.backend.Payload.Respone.ApiResponse;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully", HttpStatus.CREATED));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PaginationRequest request = new PaginationRequest(page, size, search, sort, sortDirection);
        PaginationResponse<ProductResponse> products = adminProductService.getAllProducts(request);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID productId) {
        ProductResponse product = adminProductService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse response = adminProductService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }

    @PatchMapping("/{productId}/restock")
    public ResponseEntity<ApiResponse<ProductResponse>> restockProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody RestockRequest request
    ) {
        ProductResponse response = adminProductService.restockProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product restocked successfully"));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        adminProductService.deleteProduct(productId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}