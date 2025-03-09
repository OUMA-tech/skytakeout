package com.sky.mapper;

import com.sky.entity.SetMealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    List<Long> getSetMealIdsByDishIds(List<Long> ids);

    void insertBatch(List<SetMealDish> setMealDishes);

    void deleteBatch(List<Long> ids);

    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteBySetMealId(Long id);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetMealDish> getBySetMealId(Long id);
}
