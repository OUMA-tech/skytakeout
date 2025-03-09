package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "dish management")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("save dish")
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("save dish:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("page query")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("page query dish:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("delete dish")
    public Result<String> delete(@RequestParam List<Long> ids){
        log.info("delete dishBatch:{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("get dish info by id")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("get dish info by id:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("update dish")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("update dish:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("status change")
    public Result setStatus(@PathVariable Integer status, Long id){
        log.info("status change:{}, id:{}", status, id);
        dishService.setStatus(status, id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("serchDishByCategoryId")
    public Result<List<Dish>> list(Long categoryId){
        log.info("serchDishByCategoryId:{}", categoryId);
        List<Dish> dishList = dishService.searchDishByCategoryId(categoryId);
        return Result.success(dishList);
    }

}
