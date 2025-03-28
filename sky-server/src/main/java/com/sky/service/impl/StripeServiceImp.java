package com.sky.service.impl;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.properties.StripeProperties;
import com.sky.service.OrdersService;
import com.sky.service.StripeService;
import com.sky.vo.OrderPaymentVO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collections;

@Service
public class StripeServiceImp implements StripeService {

    @Autowired
    private StripeProperties stripeSecretKey;
    @Autowired
    private OrdersService ordersService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey.getSecretKey();
    }

    @Override
    public Session createCheckoutSession(OrdersPaymentDTO ordersPaymentDTO) {
        Orders orders = ordersService.getByOrderNumber(ordersPaymentDTO.getOrderNumber());
        try {
            // TODO need to replace the url when deployed
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("aud")
                                                    .setUnitAmount(orders.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // 100元
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + orders.getNumber())
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
            return session;
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderPaymentVO paymentDetails(String sessionId) {
        Orders orders = ordersService.getBySessionId(sessionId);
        try {
            // 使用 session_id 获取 Checkout Session 信息
            Session session = Session.retrieve(sessionId);

            // 获取支付的详细信息
            long amount = session.getAmountTotal() / 100;  // 将金额转换为实际金额（单位是分）
            String currency = session.getCurrency();
            String paymentStatus = session.getPaymentStatus();
            String paymentMethod = session.getPaymentMethodTypes().get(0);  // 获取支付方式（通常是卡）

            // update orders status
            Orders ordersUpdate = new Orders();
            ordersUpdate.setId(orders.getId());
            ordersUpdate.setStatus(Orders.TO_BE_CONFIRMED);
            ordersUpdate.setPayStatus(Orders.PAID);
            ordersService.update(ordersUpdate);


            return OrderPaymentVO.builder()
                    .paymentMethod(paymentMethod)
                    .paymentStatus(paymentStatus)
                    .paymentTime(session.getCreated())
                    .amount(amount)
                    .number(orders.getNumber())
                    .currency(currency)
                    .build();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}
