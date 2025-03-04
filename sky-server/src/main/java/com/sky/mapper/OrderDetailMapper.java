package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 插入订单详情
     * @param list
     */
    void insertBatch(List<OrderDetail> list);

    /**
     * 根据订单id获得订单详情
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id=#{id};")
    List<OrderDetail> getByOrderId(Long id);


}
