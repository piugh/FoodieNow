package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.StatisticsService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //获得时期,begin-end
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        //通过datelist获得order的count
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.CANCELLED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        //封装
        String dateString = StringUtils.join(dateList, ",");
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        String turnoverString = StringUtils.join(turnoverList, ",");
        turnoverReportVO.setDateList(dateString);
        turnoverReportVO.setTurnoverList(turnoverString);

        return turnoverReportVO;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //获得datelist
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        //获得totalUserList和newUserList
        List<Integer> totalList = new ArrayList<>();
        List<Integer> newList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalList.add(totalUser == null ? 0 : totalUser);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newList.add(newUser == null ? 0 : newUser);
        }
        return UserReportVO.builder().dateList(StringUtils.join(dateList, ",")).totalUserList(StringUtils.join(totalList, ",")).newUserList(StringUtils.join(newList, ",")).build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer order = getOrder(beginTime, endTime, null);//每天订单总数
            Integer validorder = getOrder(beginTime, endTime, Orders.COMPLETED);//每天有效订单数

            orderCountList.add(order == null ? 0 : order);
            validOrderCountList.add(validorder == null ? 0 : validorder);
        }
        Integer sum = orderCountList.stream().reduce(Integer::sum).get();//时间区间内订单总数
        Integer validSum = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = sum == 0 ? 0.0 : (double) validSum / (double) sum;

        return OrderReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(sum)
                .validOrderCount(validSum)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量前十商品统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10(LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));

        List<String> nameList = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());


        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ",")).build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        //1.查询数据库获得营业数据
        LocalDateTime begin = LocalDateTime.of(LocalDate.now().minusDays(29), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.business(begin, end);

        //2.数据写入excel文件中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(is);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);//时间
            sheet1.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());//营业额
            sheet1.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());//完成率
            sheet1.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());//新增用户
            sheet1.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());//有效订单数
            sheet1.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.toLocalDate().plusDays(i);
                BusinessDataVO business = workspaceService.business(LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                sheet1.getRow(i + 7).getCell(1).setCellValue(date.toString());
                sheet1.getRow(i + 7).getCell(2).setCellValue(business.getTurnover());
                sheet1.getRow(i + 7).getCell(3).setCellValue(business.getValidOrderCount());
                sheet1.getRow(i + 7).getCell(4).setCellValue(business.getOrderCompletionRate());
                sheet1.getRow(i + 7).getCell(5).setCellValue(business.getUnitPrice());
                sheet1.getRow(i + 7).getCell(6).setCellValue(business.getNewUsers());
            }

            //3.通过输出流将文件下载到客户端浏览器
            ServletOutputStream os = response.getOutputStream();
            excel.write(os);
            //释放资源
            os.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private Integer getOrder(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("end", end);
        map.put("status", status);
        map.put("begin", begin);
        return orderMapper.countByMap(map);
    }
}
