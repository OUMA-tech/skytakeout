package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.entity.SetMeal;
import com.sky.entity.SetMealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetMealImp implements SetMealService {

//    @Override
//    public List<Setmeal> list(Setmeal setmeal) {
//        return ;
//    }
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;


    @Override
    public void saveMeal(SetMealDTO setmealDTO) {
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setmealDTO, setMeal);
        setMealMapper.insert(setMeal);
        Long setMealId = setMeal.getId();
        List<SetMealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setMealDish -> {
                setMealDish.setSetmealId(setMealId);
            });
            setMealDishMapper.insertBatch(setmealDishes);
        }

    }

    @Override
    public PageResult pageQuery(SetMealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setMealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            SetMeal setMeal = setMealMapper.getById(id);
            if (setMeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });


        setMealDishMapper.deleteBatch(ids);
        setMealMapper.deleteBatch(ids);
    }

    @Override
    public void update(SetMealDTO setmealDTO) {
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setmealDTO, setMeal);
        setMealDishMapper.deleteBySetMealId(setmealDTO.getId());
        setMealMapper.update(setMeal);

        List<SetMealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setMealDish -> {
                setMealDish.setSetmealId(setmealDTO.getId());
            });
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public SetmealVO getById(Long id) {
        SetMeal setMeal = setMealMapper.getById(id);
        if (setMeal != null){
            SetmealVO setmealVO = new SetmealVO();
            BeanUtils.copyProperties(setMeal, setmealVO);
            setmealVO.setSetmealDishes(setMealDishMapper.getBySetMealId(id));
            return setmealVO;
        }
        return null;
    }

    @Override
    public void setStatus(Integer status, Long id) {
        setMealMapper.setStatus(status, id);
    }

    @Override
    public List<SetMeal> list(SetMeal setMeal) {
        return setMealMapper.list(setMeal);
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return dishMapper.getDishItemById(id);
    }
}
