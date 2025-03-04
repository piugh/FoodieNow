package com.sky.controller.user;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@Api("C端用户相关接口")
@RequestMapping("/user/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("用户微信登陆")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO dto) {
        log.info("微信用户登陆：{}",dto.getCode());

        User user = userService.wxlogin(dto);

        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token =  JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
                );
        UserLoginVO vo = UserLoginVO.builder().id(user.getId()).openid((user.getOpenid())).token(token).build();
        return Result.success(vo);
    }

}
