package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.entity.TurnoverReport;
import com.sky.entity.UserReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {


    void insert(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders ordersUpdate);

    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);

    @Select("select * from orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    @Select("select Date(order_time) as dateList, sum(amount) as turnoverList from orders where order_time between #{begin} and #{end} and status = #{status} group by Date(order_time)")
    List<TurnoverReport> countTurnover(LocalDateTime begin, LocalDateTime end, Integer status);

    @Select("SELECT\n" +
            "    o.order_date,\n" +
            "    COUNT(DISTINCT CASE WHEN u.create_time = o.order_date THEN o.user_id END) AS new_user,\n" +
            "    COUNT(o.user_id) AS total_user\n" +
            "FROM (\n" +
            "         SELECT DATE(order_time) AS order_date, user_id\n" +
            "         FROM orders\n" +
            "         WHERE order_time BETWEEN #{begin} AND #{end} and status = #{status}\n" +
            "         GROUP BY DATE(order_time), user_id\n" +
            "     ) o\n" +
            "         JOIN user u ON o.user_id = u.id\n" +
            "GROUP BY o.order_date;")
    List<UserReport> countUser(LocalDateTime begin, LocalDateTime end, Integer status);
}
