package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "user - shopping cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    @ApiOperation("add to cart")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("add to cart:{}", shoppingCartDTO);
        cartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("get cart list")
    public Result<List<ShoppingCart>> showCartList() {
        log.info("get cart list");
        List<ShoppingCart> list = cartService.showCartList();
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    @ApiOperation("clean cart")
    public Result clean() {
        log.info("clean cart");
        cartService.clean();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("subtract from cart")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("subtract from cart:{}", shoppingCartDTO);
        cartService.sub(shoppingCartDTO);
        return Result.success();
    }
}
