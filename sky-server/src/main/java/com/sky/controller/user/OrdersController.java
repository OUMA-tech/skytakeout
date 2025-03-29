package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.service.StripeService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "user order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private StripeService stripeService;

    @PostMapping("/submit")
    @ApiOperation("submit order")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("submit order:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = ordersService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("get history orders")
    public Result<PageResult> historyOrders(int page, int pageSize, Integer status) {
        log.info("get history orders:{}，{}，{}： ", page, pageSize, status);
        PageResult pageResult = ordersService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("get order detail")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        log.info("get order detail:{}", id);
        OrderVO orderVO = ordersService.orderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("cancel order")
    public Result cancel(@PathVariable Long id) {
        log.info("cancel order:{}", id);
        ordersService.cancel(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("repetition order")
    public Result repetition(@PathVariable Long id) {
        log.info("repetition order:{}", id);
        ordersService.repetition(id);
        return Result.success();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("pay order")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("payment：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = stripeService.paymentDetails(ordersPaymentDTO);
        log.info("generate payment result：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }


    @GetMapping("/reminder/{id}")
    @ApiOperation("reminder")
    public Result reminder(@PathVariable Long id) {
        log.info("reminder order:{}", id);
        ordersService.reminder(id);
        return Result.success();
    }
}
