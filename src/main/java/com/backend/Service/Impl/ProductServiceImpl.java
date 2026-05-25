package com.backend.Service.Impl;

import com.backend.Entity.Product;
import com.backend.Mapper.ProductMapper;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Repository.ProductRepository;
import com.backend.Service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getProducts(PaginationRequest request) {
        Sort sort = Sort.by(
                request.resolvedSortDirection().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.resolvedSort()
        );
        Pageable pageable = PageRequest.of(request.resolvedPage() - 1, request.resolvedSize(), sort);

        Page<Product> page = request.resolvedSearch() != null
                ? productRepository.searchActive(request.resolvedSearch(), pageable)
                : productRepository.findAllActive(pageable);

        return PaginationResponse.of(page.map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        return productMapper.toResponse(product);
    }
}