package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface CartService {
    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showCartList();

    void clean();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
