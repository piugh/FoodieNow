package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断购物车中商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //存在则count+1；否则新建购物车
        if(list != null && list.size() > 0) {
            list.get(0).setNumber(list.get(0).getNumber()+1);
            shoppingCartMapper.update(list.get(0));
        }else{
            if(shoppingCart.getDishId()!=null) {
                //从dish表中获得图像和名字
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                SetmealVO setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中某个商品
     * @param shoppingCartDTO
     */
    @Override
    public void substarct(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart sc = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, sc);
        sc.setUserId(BaseContext.getCurrentId());
        //如果number是1.则删除，否则number-1更新
        List<ShoppingCart> list = shoppingCartMapper.list(sc);
        if(list != null && list.size() > 0) {
            if(list.get(0).getNumber() > 1){
                list.get(0).setNumber(list.get(0).getNumber()-1);
                shoppingCartMapper.update(list.get(0));
            }else{
                shoppingCartMapper.delete(list.get(0));
            }
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        ShoppingCart sc = ShoppingCart.builder().id(BaseContext.getCurrentId()).build();
        List<ShoppingCart> list = shoppingCartMapper.list(sc);
        return list;
    }

}
