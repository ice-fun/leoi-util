package com.github.leoiutil.core.annotation;


import java.lang.annotation.*;

/**
 * AOP系统日志模块 织入注解
 * 添加此注解（controller），实现基于aop的日志收集功能
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AopLog {
    boolean value() default true;

    String apiName() default "";

    String appPlatform() default "";
}
