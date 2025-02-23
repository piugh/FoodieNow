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
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //菜品添加一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        //口味添加数据
        List<DishFlavor> list = dishDTO.getFlavors();
        for (DishFlavor dishFlavor : list) {
            dishFlavor.setDishId(dishId);
        }
        if (list != null && list.size() > 0) {
            dishFlavorMapper.insertBatch(list);
        }
    }

    /**
     * 菜品分页查询
     * @param dishQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishQueryDTO) {
        PageHelper.startPage(dishQueryDTO.getPage(), dishQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            //不在起售中
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
            //不在套餐中
            if (setmealIds != null && setmealIds.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }

        dishMapper.deleteByIds(ids);

        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //菜品信息
        Dish dish = dishMapper.getById(id);
        //口味信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品信息和对应口味信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //删除口味
        List<Long> id = new ArrayList<>();
        id.add(dish.getId());
        dishFlavorMapper.deleteByDishIds(id);
        //更新口味
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if (dishFlavors != null && dishFlavors.size() > 0) {
            for (DishFlavor dishFlavor : dishFlavors) {
                dishFlavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }
}
