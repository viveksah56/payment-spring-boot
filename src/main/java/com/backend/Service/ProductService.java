package com.backend.Service;

import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Respone.PaginationResponse;

import java.util.UUID;

public interface ProductService {
    PaginationResponse<ProductResponse> getProducts(PaginationRequest request);
    ProductResponse getProduct(UUID productId);
}