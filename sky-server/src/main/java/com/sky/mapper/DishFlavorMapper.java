package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量新建菜品口味
     * @param list
     */
    void insertBatch(List<DishFlavor> list);

    /**
     * 根据菜品id批量删除口味
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id查询对应口味数据
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId};")
    List<DishFlavor> getByDishId(Long dishId);
}
