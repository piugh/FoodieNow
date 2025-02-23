package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套参数量
     * @param categoryID
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countBycategoryID(Long categoryID);
}
