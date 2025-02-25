package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 新建套餐数据
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 批量删除套参
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新套餐信息
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 套餐分页查询
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO queryDTO);

    /**
     * 根据分类id查询套参数量
     * @param categoryID
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countBycategoryID(Long categoryID);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select s.*, (c.name)categoryName from setmeal s left join category c on s.category_id = c.id " +
            "where s.id = #{id}")
    SetmealVO getById(Long id);

}
