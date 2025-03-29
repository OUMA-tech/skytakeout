//package com.sky.controller.admin;
//
//import com.sky.dto.OrdersPaymentDTO;
//import com.sky.service.StripeService;
//import com.stripe.model.checkout.Session;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Collections;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/admin/payment")
//@Slf4j
//public class StripeCheckoutController {
//
//    @Autowired
//    private StripeService stripeService;
//
//    @PostMapping("/create-checkout-session")
//    public ResponseEntity<Map> createCheckoutSession(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
//        log.info("making payment：{}", ordersPaymentDTO);
//
//        Session session = stripeService.createCheckoutSession(ordersPaymentDTO);
//
//        log.info("session created：{}", session.getId());
//        // 返回Checkout页面的URL
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON) // 改为 JSON 格式
//                .body(Collections.singletonMap("sessionId", session.getId()));
//    }
//
//}