package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    // checking order unpaid over 15 minutes
    // TODO switch back to 0 * * * * ?
    @Scheduled(cron = "0 0 1 * * ?") // every minute
    public void autoCancelOrder(){
        log.info("auto cancel order if order unpaid over 15 minutes");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && !ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                Orders ordersUpdate = Orders.builder()
                        .id(orders.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("auto cancel order because order unpaid over 15 minutes")
                        .cancelTime(LocalDateTime.now())
                        .build();
                ordersMapper.update(ordersUpdate);
            }
        }
    }

    // checking order confirmed over 1 day
    @Scheduled(cron = "0 0 1 * * ?")    // checking at 1 am every day
    public void autoConfirmOrder(){
        log.info("auto confirm order if order paid over 1 day");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                Orders ordersUpdate = Orders.builder()
                        .id(orders.getId())
                        .status(Orders.COMPLETED)
                        .build();
                ordersMapper.update(ordersUpdate);
            }
        }
    }
}
