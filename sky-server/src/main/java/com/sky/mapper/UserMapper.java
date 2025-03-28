package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    @Select("SELECT COUNT(*) from user where create_time between #{begin} and #{end}")
    Integer countByMap(Map map);

    @Select("select * from user where username = #{username}")
    User getByOpenUsername(String username);
}
