package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    void deleteBatch(List<Long> dishIds);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    @Update("update dish set status = #{status} where id = #{id}")
    void setStatus(Integer status, Long id);

    List<Dish> list(Dish dish);

    @Select("select sd.name,sd.copies,d.image,d.descrepetion from setmeal_dish sd" +
            "left join dish d on sd.dish_id = d.id where sd.setmeal_id = #{setMealId}")
    List<DishItemVO> getDishItemById(Long setMealId);
}
