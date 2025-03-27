package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.service.OrdersService;
import com.sky.service.StripeService;
import com.sky.vo.OrderVO;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class StripeCheckoutController {

    @Autowired
    private StripeService stripeService;
    @Autowired
    private OrdersService ordersService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        Orders orders = ordersService.getByOrderNumber(ordersPaymentDTO.getOrderNumber());
        try {
            // 创建Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:8080/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("aud")
                                                    .setUnitAmount(orders.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // 100元
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + orders.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            Orders ordersUpdate = new Orders();
            ordersUpdate.setSessionId(session.getId());
            ordersUpdate.setId(orders.getId());
            ordersService.update(ordersUpdate);

            // 返回Checkout页面的URL
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN) // 关键：声明内容类型为纯文本
                    .body(session.getUrl());
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("创建支付会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> success(@RequestParam("session_id") String sessionId) {
        Orders orders = ordersService.getBySessionId(sessionId);
        try {
            // 使用 session_id 获取 Checkout Session 信息
            Session session = Session.retrieve(sessionId);

            // 获取支付的详细信息
            long amount = session.getAmountTotal() / 100;  // 将金额转换为实际金额（单位是分）
            String currency = session.getCurrency();
            String paymentStatus = session.getPaymentStatus();
            String paymentMethod = session.getPaymentMethodTypes().get(0);  // 获取支付方式（通常是卡）

            Orders ordersUpdate = new Orders();
            ordersUpdate.setId(orders.getId());
            ordersUpdate.setStatus(Orders.TO_BE_CONFIRMED);
            ordersUpdate.setPayStatus(Orders.PAID);
            ordersService.update(ordersUpdate);
            // 构建返回的数据
            Map<String, Object> response = new HashMap<>();
            response.put("amount", amount);
            response.put("currency", currency);
            response.put("status", paymentStatus);
            response.put("payment_method", paymentMethod);

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to fetch payment details"));
        }
    }


    @GetMapping("/cancel")
    public String paymentCancel() {
        return "payment-cancel"; // 支付取消页面
    }
}