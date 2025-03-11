package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("admin order controller")
@RequestMapping("/admin/order")
@Api(tags = "order management")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    @GetMapping("/conditionSearch")
    @ApiOperation("search order")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("search order:{}", ordersPageQueryDTO);
        PageResult pageResult = ordersService.adminConditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistcs")
    @ApiOperation("statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("statistics");
        OrderStatisticsVO orderStatisticsVO = ordersService.adminStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("search order details")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("search order details:{}", id);
        OrderVO orderVO = ordersService.orderDetail(id);
        return Result.success(orderVO);
    }
}
