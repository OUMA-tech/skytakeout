package com.sky.controller.admin;

import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @PostMapping
    @ApiOperation("save meal")
    public Result saveMeal(@RequestBody SetMealDTO setmealDTO) {
        log.info("save setmeal:{}", setmealDTO);
        setMealService.saveMeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("page query")
    public Result<PageResult> pageQuery(SetMealPageQueryDTO setmealPageQueryDTO) {
        log.info("page query:{}", setmealPageQueryDTO);
        PageResult pageResult = setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("delete meal")
    public Result deleteBatch(@RequestParam List<Long> ids) {
        log.info("delete setmeal:{}", ids);
        setMealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("get meal by id")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("get setmeal by id:{}", id);
        return Result.success(setMealService.getById(id));
    }

    @PutMapping
    @ApiOperation("update meal")
    public Result update(@RequestBody SetMealDTO setmealDTO) {
        log.info("update setmeal:{}", setmealDTO);
        setMealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("status change")
    public Result setStatus(@PathVariable Integer status, Long id) {
        log.info("status change:{}", status);
        setMealService.setStatus(status, id);
        return Result.success();
    }
}
