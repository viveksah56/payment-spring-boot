package com.backend.Service.Impl;

import com.backend.Entity.Product;
import com.backend.Mapper.ProductMapper;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Request.ProductRequest.CreateProductRequest;
import com.backend.Payload.Request.ProductRequest.RestockRequest;
import com.backend.Payload.Request.ProductRequest.UpdateProductRequest;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Repository.ProductRepository;
import com.backend.Service.AdminProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .discount(request.discount())
                .stock(request.stock())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {} [{}]", saved.getName(), saved.getProductId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        if (request.name() != null)        product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.price() != null)       product.setPrice(request.price());
        if (request.discount() != null)    product.setDiscount(request.discount());
        if (request.stock() != null)       product.setStock(request.stock());

        Product saved = productRepository.save(product);
        log.info("Product updated: {} [{}]", saved.getName(), saved.getProductId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse restockProduct(UUID productId, RestockRequest request) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        int before = product.getStock();
        product.setStock(before + request.quantity());

        Product saved = productRepository.save(product);
        log.info("Product restocked: {} [{}] {} -> {}", saved.getName(), saved.getProductId(), before, saved.getStock());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId, String deletedBy) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        product.setDeleted(true);
        product.setDeletedAt(Instant.now());
        product.setDeletedBy(deletedBy);

        productRepository.save(product);
        log.info("Product soft-deleted: {} [{}] by {}", product.getName(), productId, deletedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductResponse> getAllProducts(PaginationRequest request) {
        Sort sort = Sort.by(
                request.resolvedSortDirection().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.resolvedSort()
        );
        Pageable pageable = PageRequest.of(request.resolvedPage() - 1, request.resolvedSize(), sort);

        Page<Product> page = request.resolvedSearch() != null
                ? productRepository.searchAll(request.resolvedSearch(), pageable)
                : productRepository.findAll(pageable);

        return PaginationResponse.of(page.map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        return productMapper.toResponse(product);
    }
}