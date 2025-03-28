package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserMapper userMapper;

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    private final WeChatProperties weChatProperties;

    public UserServiceImp(WeChatProperties weChatProperties) {
        this.weChatProperties = weChatProperties;
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {

        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        User user = userMapper.getByOpenUsername(username);
        if (user == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        } else if (!Objects.equals(password, user.getPassword())) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // return user
        return user;
    }

    @Override
    public void userRegister(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        User user = userMapper.getByOpenUsername(username);
        if (user != null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User userInsert = User.builder()
                .username(username)
                .password(password)
                .build();
        userMapper.insert(userInsert);
    }

    private String getOpenId(String code){
        // user weChat api
        Map<String,String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN,map);

        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
