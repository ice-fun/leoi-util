package com.github.leoiutil.core.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 自定义注解，用于controller接口方法上，添加后，此接口允许未登录的游客访问(即无token)
 * 属性{@code authentication}, 默认为{@code true },当用户传入token时 校验此token是否合法。
 * 如果为{@code false },则 当用户传入token时 不校验token，相当于放行此接口，不做任何拦截处理
 **/

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AllowVisitor {
    boolean authentication() default true;
}
