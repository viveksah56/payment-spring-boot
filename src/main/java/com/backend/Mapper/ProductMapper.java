package com.backend.Mapper;

import com.backend.Entity.Product;
import com.backend.Payload.Respone.OrderResponse.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        double effectivePrice = computeEffectivePrice(product);
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscount(),
                effectivePrice,
                product.getStock()
        );
    }

    public double computeEffectivePrice(Product product) {
        if (product.getDiscount() == null || product.getDiscount() <= 0) {
            return product.getPrice();
        }
        return product.getPrice() - (product.getPrice() * product.getDiscount() / 100.0);
    }
}