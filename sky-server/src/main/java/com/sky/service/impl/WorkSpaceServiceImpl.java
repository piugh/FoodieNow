package com.sky.service.impl;

import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 今日数据总览
     * @return
     */
    @Override
    public BusinessDataVO business(LocalDateTime begin, LocalDateTime end) {
        //查询orders表
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        Integer orderCount = orderMapper.countByMap(map);//所有订单数
        orderCount = orderCount == null ? 0 : orderCount;
        map.put("status", 5);
        Integer validOrderCount = orderMapper.countByMap(map);//有效订单数
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;
        Double orderCompletionRate = orderCount == 0 ? 0.0 : (double) validOrderCount / (double)orderCount;//订单完成率
        Double turnover = orderMapper.sumByMap(map);//营业额
        turnover = turnover == null ? 0.0 : turnover;
        Double unitPrice = validOrderCount == 0 ? 0 : turnover / validOrderCount;//平均客单价
        //查询user表
        Integer newUsers = userMapper.countByMap(map);
        return BusinessDataVO.builder()
                .turnover(turnover)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 订单总览
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        Integer waitingOrders = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer completedOrders = orderMapper.countByStatus(Orders.COMPLETED);
        Integer cancelledOrders = orderMapper.countByStatus(Orders.CANCELLED);
        Integer allOrders = orderMapper.countByMap(new HashMap());
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        Dish dish = Dish.builder().status(1).build();//1起售
        Integer sold = dishMapper.list(dish).size();
        dish.setStatus(0);//0停售
        Integer discontinued = dishMapper.list(dish).size();
        return DishOverViewVO.builder().sold(sold).discontinued(discontinued).build();
    }

    /**
     * 套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Setmeal setmeal = Setmeal.builder().status(1).build();
        Integer sold = setmealMapper.list(setmeal).size();
        setmeal.setStatus(0);
        Integer discontinued = setmealMapper.list(setmeal).size();
        return SetmealOverViewVO.builder().sold(sold).discontinued(discontinued).build();
    }
}
