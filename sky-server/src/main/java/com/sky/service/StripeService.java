package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderPaymentVO;
import com.stripe.model.checkout.Session;

public interface StripeService {
    String createCheckoutSession(Long amount, String orderNumber, int payMethod);

    OrderPaymentVO paymentDetails(OrdersPaymentDTO ordersPaymentDTO);
}
