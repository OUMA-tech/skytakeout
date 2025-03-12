package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

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
}
