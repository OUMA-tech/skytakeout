package com.sky.service.impl;

import com.sky.properties.StripeProperties;
import com.sky.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class StripeServiceImp implements StripeService {
    @Service
    public class StripeService {

        @Autowired
        private StripeProperties stripeSecretKey;

        @PostConstruct
        public void init() {
            Stripe.apiKey = stripeSecretKey.getSecretKey();
        }

        public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
            return PaymentIntent.retrieve(paymentIntentId); // 查询 PaymentIntent
        }
    }
}
