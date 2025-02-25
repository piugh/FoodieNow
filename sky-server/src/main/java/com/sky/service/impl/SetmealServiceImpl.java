package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新建套餐以及套餐菜品关联数据
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //1. 新建套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        //2.新建套餐-菜品关联数据-需要套餐id
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        for(SetmealDish sd : list){
            sd.setSetmealId(setmealId);
        }
        setmealDishMapper.insertBatch(list);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for(Long id : ids){
            //套餐售卖状态非起售中-status=1
            SetmealVO setmeal = setmealMapper.getById(id);
            if(setmeal != null && setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //1. 删除套餐数据
        setmealMapper.deleteByIds(ids);
        //2. 删除套餐菜品数据-根据套餐id删除套餐菜品数据
        setmealDishMapper.deleteBySetmealIds(ids);

    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //1.修改套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //2. 删除套餐菜品关联数据
        Long id = setmeal.getId();
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        setmealDishMapper.deleteBySetmealIds(ids);
        //3. 新建套餐菜品关联数据
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        for(SetmealDish sd : list){
            sd.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatch(list);
    }

    /**
     * 套餐起售/停售
     * @param status
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 套餐分页查询
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO vo = setmealMapper.getById(id);
        //获得套餐菜品关联数据
        List<SetmealDish> list = setmealDishMapper.getSetmealDishs(id);
        vo.setSetmealDishes(list);
        return vo;
    }
}
