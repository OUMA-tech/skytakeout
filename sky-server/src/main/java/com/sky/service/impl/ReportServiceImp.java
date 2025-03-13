package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.TurnoverReport;
import com.sky.entity.UserReport;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImp implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<TurnoverReport> turnoverList = ordersMapper.countTurnover(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX),
                Orders.COMPLETED);
        log.info("turnoverList: {}",turnoverList);
        String dateList = turnoverList.stream().map(turnoverReport -> turnoverReport.getDateList().toString())
                .collect(Collectors.joining(","));
        String turnoverListstr = turnoverList.stream().map(turnoverReport -> turnoverReport.getTurnoverList().toString())
                .collect(Collectors.joining(","));
        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverListstr)
                .build();
    }

    @Override
    public UserReportVO userStastistics(LocalDate begin, LocalDate end) {
        List<UserReport> userReportList = ordersMapper.countUser(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX),
                Orders.COMPLETED);
        log.info("userReportList: {}",userReportList);
        String dateList = userReportList.stream().map(userReport -> userReport.getOrderDate().toString())
                .collect(Collectors.joining(","));
        String totalUserList = userReportList.stream().map(userReport -> userReport.getTotalUser().toString())
                .collect(Collectors.joining(","));
        String newUserList = userReportList.stream().map(userReport -> userReport.getNewUser().toString())
                .collect(Collectors.joining(","));
        return UserReportVO.builder()
                .dateList(dateList)
                .totalUserList(totalUserList)
                .newUserList(newUserList)
                .build();

    }
}
