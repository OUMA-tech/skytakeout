package com.sky.service;

import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetMealService {
    void saveMeal(SetMealDTO setmealDTO);

    PageResult pageQuery(SetMealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

//    List<Setmeal> list(Setmeal setmeal);
}
