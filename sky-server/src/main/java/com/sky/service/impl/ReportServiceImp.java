package com.sky.service.impl;

import com.sky.entity.*;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class ReportServiceImp implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<TurnoverReport> turnoverList = ordersMapper.countTurnover(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX),
                Orders.COMPLETED);
        log.info("turnoverList: {}",turnoverList);
        if (turnoverList != null && !turnoverList.isEmpty()) {
            StringJoiner dateJoiner = new StringJoiner(",");
            StringJoiner turnoverJoiner = new StringJoiner(",");

            turnoverList.forEach(turnoverReport -> {
                dateJoiner.add(turnoverReport.getDateList().toString());
                turnoverJoiner.add(turnoverReport.getTurnoverList().toString());
            });

            return TurnoverReportVO.builder()
                    .dateList(dateJoiner.toString())
                    .turnoverList(turnoverJoiner.toString())
                    .build();
        }
        return null;
    }

    @Override
    public UserReportVO userStastistics(LocalDate begin, LocalDate end) {
        List<UserReport> userReportList = ordersMapper.countUser(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX),
                Orders.COMPLETED);
        log.info("userReportList: {}",userReportList);
        if (userReportList != null && !userReportList.isEmpty()){
            StringJoiner dateJoiner = new StringJoiner(",");
            StringJoiner totalUserJoiner = new StringJoiner(",");
            StringJoiner newUserJoiner = new StringJoiner(",");

            userReportList.forEach(userReport -> {
                dateJoiner.add(userReport.getOrderDate().toString());
                totalUserJoiner.add(userReport.getTotalUser().toString());
                newUserJoiner.add(userReport.getNewUser().toString());
            });

            return UserReportVO.builder()
                    .dateList(dateJoiner.toString())
                    .totalUserList(totalUserJoiner.toString())
                    .newUserList(newUserJoiner.toString())
                    .build();
        }

        return null;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<OrderReport> ordersStatistics = ordersMapper.ordersStatistics(
                beginTime,
                endTime,
                Orders.COMPLETED);
        log.info("ordersStatistics: {}",ordersStatistics);
        Integer totalOrderCount = ordersMapper.countAllByDate(beginTime, endTime);
        Integer validOrderCount = ordersMapper.countByStatusAndDate(beginTime, endTime, Orders.COMPLETED);
        log.info("totalOrderCount: {}, validOrderCount: {}",totalOrderCount,validOrderCount);
        if (ordersStatistics != null && !ordersStatistics.isEmpty()){
            StringJoiner dateJoiner = new StringJoiner(",");
            StringJoiner orderCountJoiner = new StringJoiner(",");
            StringJoiner validOrderCountJoiner = new StringJoiner(",");
            for (OrderReport orderReport : ordersStatistics) {
                dateJoiner.add(orderReport.getDateList().toString());
                orderCountJoiner.add(orderReport.getOrderCountList().toString());
                validOrderCountJoiner.add(orderReport.getValidOrderCountList().toString());
            }

            return OrderReportVO.builder()
                    .totalOrderCount(totalOrderCount)
                    .validOrderCount(validOrderCount)
                    .orderCompletionRate(validOrderCount.doubleValue() / totalOrderCount.doubleValue())
                    .dateList(dateJoiner.toString())
                    .orderCountList(orderCountJoiner.toString())
                    .validOrderCountList(validOrderCountJoiner.toString())
                    .build();

        }
        return null;
    }

    @Override
    public SalesTop10ReportVO top10Dishes(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<SalesTop10Report> top10Dishes = ordersMapper.countTop10Dishes(beginTime, endTime, Orders.COMPLETED);
        log.info("top10Dishes: {}",top10Dishes);
        if (top10Dishes != null && !top10Dishes.isEmpty()){
            StringJoiner nameJoiner = new StringJoiner(",");
            StringJoiner numberJoiner = new StringJoiner(",");
            for (SalesTop10Report salesTop10Report : top10Dishes) {
                nameJoiner.add(salesTop10Report.getNameList().toString());
                numberJoiner.add(salesTop10Report.getNumberList().toString());
            }
            return SalesTop10ReportVO.builder()
                    .nameList(nameJoiner.toString())
                    .numberList(numberJoiner.toString())
                    .build();
        }
        return null;
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
        // query database get recently 30 days data
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);

        // using poi to export data to xlsx
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/template.xlsx");
        try {
            assert inputStream != null;
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("Time:" + begin + " to " + end);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // details
            for(int i = 0; i < 30; i++){
                LocalDate date = begin.plusDays(i);
                BusinessDataVO businessDataVO1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet.getRow(7 + i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7 + i).getCell(2).setCellValue(businessDataVO1.getTurnover());
                sheet.getRow(7 + i).getCell(3).setCellValue(businessDataVO1.getValidOrderCount());
                sheet.getRow(7 + i).getCell(4).setCellValue(businessDataVO1.getOrderCompletionRate());
                sheet.getRow(7 + i).getCell(5).setCellValue(businessDataVO1.getUnitPrice());
                sheet.getRow(7 + i).getCell(6).setCellValue(businessDataVO1.getNewUsers());
            }

            // return excel file
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            // close excel
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
