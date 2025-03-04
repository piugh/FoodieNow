package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api("工作台相关接口")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    @ApiOperation("今日数据统计")
    public Result<BusinessDataVO> business(){
        log.info("工作台今日数据统计");
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return Result.success(workspaceService.business(begin, end));
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("工作台订单总览")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("工作台订单总览");
        return Result.success(workspaceService.overviewOrders());
    }

    @GetMapping("/overviewDishes")
    @ApiOperation("工作台菜品总览")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("工作台菜品总览");
        return Result.success(workspaceService.overviewDishes());
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("工作台套餐总览")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("工作台套餐总览");
        return Result.success(workspaceService.overviewSetmeals());
    }

}
