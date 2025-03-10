package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DishServiceImp implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetMealDishMapper setMealDishMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

        // refresh cache
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        // dish can be delete
        // 1. dish in sale
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2. dish in meals
        List<Long> setMealIds = setMealDishMapper.getSetMealIdsByDishIds(ids);
        if(setMealIds != null && !setMealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        for(Long id : ids){
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatch(ids);

        // refresh cache
        cleanCache("dish_*");
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        if(dish != null){
            List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVO.setFlavors(dishFlavors);
            return dishVO;
        }
        return null;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // delete all original flavor then set up new flavor
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        dishMapper.update(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

        // refresh cache
        cleanCache("dish_*");
    }

    @Override
    public void setStatus(Integer status, Long id) {
        dishMapper.setStatus(status, id);

        // refresh cache
        cleanCache("dish_*");
    }

    @Override
    public List<Dish> searchDishByCategoryId(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        List<Dish> dishList = dishMapper.list(dish);
        return dishList;
    }

    @Override
    public List<DishVO> listWithFlavor(Long categoryId) {
        List<Dish> dishList = searchDishByCategoryId(categoryId);
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish dish : dishList){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);

            List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(dish.getId());
            dishVO.setFlavors(dishFlavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    @Override
    public List<DishVO> listWithFlavorUser(Long categoryId) {

        String key = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && !list.isEmpty()) {
            return list;
        }

        list = listWithFlavor(categoryId);
        redisTemplate.opsForValue().set(key, list);

        return list;
    }

    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
