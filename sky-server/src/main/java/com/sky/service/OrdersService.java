package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.stripe.exception.StripeException;

public interface OrdersService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult historyOrders(int page, int pageSize, Integer status);

    OrderVO orderDetail(Long id);

    void cancel(Long id);

    void repetition(Long id);

    PageResult adminConditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO adminStatistics();

    void adminConfirmOrder(OrdersDTO ordersDTO);

    void adminRejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);

    void adminDeliveryOrder(Long id);

    void adminCompleteOrder(Long id);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws StripeException;

    void paySuccess(String outTradeNo);

    void reminder(Long id);


    void update(Orders orders);

    Orders getBySessionId(String sessionId);

    Orders getByOrderNumber(String orderNumber);
}
