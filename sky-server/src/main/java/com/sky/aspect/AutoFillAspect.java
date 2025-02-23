package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段填充
 */

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinpoint) {
        log.info("开始进行公共字段自动填充");

        //获得注解中value（insert or update）
        MethodSignature signature = (MethodSignature) joinpoint.getSignature();
        OperationType value = signature.getMethod().getAnnotation(AutoFill.class).value();

        //获得传入形参
        Object[] args = joinpoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //形参设置
        switch (value) {
            case UPDATE:
                try {
                    Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);
                    break;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            case INSERT:
                try {
                    Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);
                    setCreateTime.invoke(entity, now);
                    setCreateUser.invoke(entity, currentId);
                    break;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


        }


    }


}
