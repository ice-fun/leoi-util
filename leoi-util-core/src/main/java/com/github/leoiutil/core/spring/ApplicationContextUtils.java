package com.github.leoiutil.core.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author lyb
 * spring 上下文工具类
 **/


public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.context = applicationContext;
    }

    public static <T> T getSpringBean(String name) {
        return (T) context.getBean(name);
    }

    public static <T> T getSpringBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static <T> T getSpringBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    /**
     * 根据注解获取bean
     * @param annotationClass 注解 class
     * @return m
     */
    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) {
        return context.getBeansWithAnnotation(annotationClass);
    }

    /**
     * 动态注册Bean
     *
     * @param <T>      T
     * @param beanName 名称
     * @param bean     bean
     */
    public static <T> void registerBean(String beanName, T bean) {
        ConfigurableApplicationContext context1 = (ConfigurableApplicationContext) context;
        context1.getBeanFactory().registerSingleton(beanName, bean);
    }
}
