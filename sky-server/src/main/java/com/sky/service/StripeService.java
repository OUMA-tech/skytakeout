package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderPaymentVO;
import com.stripe.model.checkout.Session;

public interface StripeService {
    Session createCheckoutSession(OrdersPaymentDTO ordersPaymentDTO);

    OrderPaymentVO paymentDetails(String sessionId);
}
