package com.sky.annotation;

/**
 * 自定义注解：用于表示方法，进行公共字段填充处理
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface AutoFill {
    //数据库操作类型：update和insert
    OperationType value();
}
