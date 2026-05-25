package com.backend.Service.Impl;

import com.backend.Entity.*;
import com.backend.Enum.OrderStatus;
import com.backend.Enum.PaymentProvider;
import com.backend.Enum.PaymentStatus;
import com.backend.Mapper.OrderMapper;
import com.backend.Payload.Request.OrderRequest.CartItemRequest;
import com.backend.Payload.Request.OrderRequest.CheckoutRequest;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Request.PaymentRequestDTO;
import com.backend.Payload.Respone.OrderResponse.*;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Payload.Respone.PaymentResponseDTO;
import com.backend.Repository.OrderRepository;
import com.backend.Repository.PaymentRepository;
import com.backend.Repository.ProductRepository;
import com.backend.Repository.UserRepository;
import com.backend.Service.OrderService;
import com.backend.Service.PaymentGateway;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final Map<String, PaymentGateway> paymentGateways;

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<OrderItem> items = new ArrayList<>();
        double subtotal = 0.0;
        double totalDiscount = 0.0;

        for (CartItemRequest cartItem : request.items()) {
            Product product = productRepository.findActiveById(cartItem.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + cartItem.productId()));

            if (product.getStock() < cartItem.quantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            double unitPrice = product.getPrice();
            double discountApplied = 0.0;

            if (product.getDiscount() != null && product.getDiscount() > 0) {
                discountApplied = unitPrice * product.getDiscount() / 100.0;
            }

            double effectivePrice = unitPrice - discountApplied;
            double lineTotal = effectivePrice * cartItem.quantity();
            double lineDiscount = discountApplied * cartItem.quantity();

            subtotal += unitPrice * cartItem.quantity();
            totalDiscount += lineDiscount;

            product.setStock(product.getStock() - cartItem.quantity());
            productRepository.save(product);

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.quantity())
                    .unitPrice(unitPrice)
                    .discountApplied(discountApplied)
                    .lineTotal(lineTotal)
                    .build());
        }

        double total = subtotal - totalDiscount;

        Order order = Order.builder()
                .user(user)
                .subtotal(subtotal)
                .discountAmount(totalDiscount)
                .total(total)
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        PaymentGateway gateway = resolveGateway(request.provider());
        PaymentResponseDTO providerResponse = gateway.createPayment(new PaymentRequestDTO(
                total,
                request.currency(),
                request.provider(),
                user.getEmail(),
                user.getUserId()
        ));

        Payment payment = Payment.builder()
                .order(savedOrder)
                .user(user)
                .providerOrderId(providerResponse.providerOrderId())
                .clientSecret(providerResponse.clientSecret())
                .amount(total)
                .currency(request.currency())
                .provider(request.provider())
                .status(PaymentStatus.PENDING)
                .customerEmail(user.getEmail())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        savedOrder.setPayment(savedPayment);

        return orderMapper.toCheckoutResponse(savedOrder, savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<OrderSummaryResponse> getUserOrders(String userEmail, PaginationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = buildPageable(request);
        Page<Order> page = orderRepository.findByUserId(user.getUserId(), pageable);
        return PaginationResponse.of(page.map(orderMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryResponse getOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Access denied to order: " + orderId);
        }

        return orderMapper.toSummaryResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PaymentHistoryResponse> getUserPayments(String userEmail, PaginationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = buildPageable(request);
        Page<Payment> page = paymentRepository.findByUserId(user.getUserId(), pageable);
        return PaginationResponse.of(page.map(orderMapper::toPaymentHistoryResponse));
    }

    private PaymentGateway resolveGateway(PaymentProvider provider) {
        return switch (provider) {
            case STRIPE -> paymentGateways.get("stripeGateway");
            case RAZORPAY -> paymentGateways.get("razorpayGateway");
            case COD -> paymentGateways.get("codGateway");
        };
    }

    private Pageable buildPageable(PaginationRequest request) {
        Sort sort = Sort.by(
                request.resolvedSortDirection().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.resolvedSort()
        );
        return PageRequest.of(request.resolvedPage() - 1, request.resolvedSize(), sort);
    }
}