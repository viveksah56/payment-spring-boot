package com.backend.Service;

import com.backend.Payload.Request.ProductRequest.CreateProductRequest;
import com.backend.Payload.Request.ProductRequest.RestockRequest;
import com.backend.Payload.Request.ProductRequest.UpdateProductRequest;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.PaginationResponse;

import java.util.UUID;

public interface AdminProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(UUID productId, UpdateProductRequest request);
    ProductResponse restockProduct(UUID productId, RestockRequest request);
    void deleteProduct(UUID productId, String deletedBy);
    PaginationResponse<ProductResponse> getAllProducts(PaginationRequest request);
    ProductResponse getProduct(UUID productId);
}