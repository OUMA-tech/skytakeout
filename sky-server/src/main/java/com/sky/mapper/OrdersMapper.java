package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Select("select\n" +
            "    DATE(order_time) as date_list,\n" +
            "    COUNT(*) as order_count_list,\n" +
            "    COUNT(case when status = #{status} then 1 end) as valid_order_count_list\n" +
            "from orders\n" +
            "where order_time BETWEEN #{begin} AND #{end}\n" +
            "group by date_list;")
    List<OrderReport> ordersStatistics(LocalDateTime begin, LocalDateTime end, Integer status);

    @Select("select count(*) from orders where order_time BETWEEN #{begin} AND #{end}")
    Integer countAllByDate(LocalDateTime begin, LocalDateTime end);

    @Select("select count(*) from orders where order_time BETWEEN #{begin} AND #{end} and status = #{status}")
    Integer countByStatusAndDate(LocalDateTime begin, LocalDateTime end, Integer status);

    @Select("select od.name as name_list, sum(od.number) as number_list\n" +
            "FROM order_detail od\n" +
            "         JOIN orders o ON od.order_id = o.id\n" +
            "WHERE o.order_time BETWEEN #{begin} AND #{end} and status = #{status}\n" +
            "group by name_list\n" +
            "order by number_list desc\n" +
            "limit 10;")
    List<SalesTop10Report> countTop10Dishes(LocalDateTime begin, LocalDateTime end, Integer status);

    Integer countByMap(Map map);

    @Select("select sum(amount) from orders where order_time BETWEEN #{begin} AND #{end} and status = #{status}")
    Double sumByMap(Map map);

    @Select("select * from orders where session_id = #{sessionId}")
    Orders getBySessionId(String sessionId);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByOrderNumber(String orderNumber);
}
