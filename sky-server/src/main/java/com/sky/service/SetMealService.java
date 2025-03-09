package com.sky.service;

import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.entity.SetMeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetMealService {
    void saveMeal(SetMealDTO setmealDTO);

    PageResult pageQuery(SetMealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void update(SetMealDTO setmealDTO);

    SetmealVO getById(Long id);

    void setStatus(Integer status, Long id);

    List<SetMeal> list(SetMeal setmeal);

    List<DishItemVO> getDishItemById(Long id);

//    List<Setmeal> list(Setmeal setmeal);
}
