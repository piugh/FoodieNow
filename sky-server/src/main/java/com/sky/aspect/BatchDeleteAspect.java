package com.sky.aspect;


import com.sky.constant.MessageConstant;
import com.sky.exception.DeletionNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Slf4j
@Component
public class BatchDeleteAspect {
    //1. 切入点设置
    @Pointcut("execution(* com.sky.controller.admin.*.*(..)) && @annotation(com.sky.annotation.BatchDelet)")
    public void batchDeletePointcut() {}
    //2.通知设置
    @Before("batchDeletePointcut()")
    public void batchDelete(JoinPoint joinPoint) {
        log.info("批量删除前检查");
        List args = (List) joinPoint.getArgs()[0];
        if (args == null || args.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DELETE_DATA_NULL);
        }
    }

}
