package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.service.StripeService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/payment")
@Slf4j
public class StripeCheckoutController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map> createCheckoutSession(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("making payment：{}", ordersPaymentDTO);

        Session session = stripeService.createCheckoutSession(ordersPaymentDTO);

        log.info("session created：{}", session.getId());
        // 返回Checkout页面的URL
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON) // 改为 JSON 格式
                .body(Collections.singletonMap("sessionId", session.getId()));
    }

    @GetMapping("/paymentDetails")
    public Result<OrderPaymentVO> paymentDetails(@RequestParam("session_id") String sessionId) {
        OrderPaymentVO response = stripeService.paymentDetails(sessionId);
        return Result.success(response);
    }

}