package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReport implements Serializable {

    //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
    private LocalDate dateList;

    //每日订单数，以逗号分隔，例如：260,210,215
    private BigInteger orderCountList;

    //每日有效订单数，以逗号分隔，例如：20,21,10
    private BigInteger validOrderCountList;
}

