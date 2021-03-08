package com.github.leoiutil.core.aop;

import com.github.leoiutil.core.annotation.AopLog;
import com.github.leoiutil.core.aop.interfaces.IAopLogService;
import com.github.leoiutil.core.aop.interfaces.IAopLogger;
import com.github.leoiutil.core.aop.interfaces.ILogAuthAccount;
import com.github.leoiutil.core.ip.IpUtils;
import com.github.leoiutil.core.json.JSONUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author lyb
 * <p>
 * 基于 AOP 的日志收集模块
 * 根据注解 {@code AopLog} 织入，实现对接口的日志收集。包含接口路径，用户信息，ip地址，请求参数，响应结果，异常信息
 * 通过spring@Bean的方式装配到系统中
 * 可以自定义 成功、识别 异常 文字类型
 * 可以选择保存日志到数据库 还是 elasticsearch
 */
@Aspect
public class SystemLogAspect<T extends IAopLogger> {

    private final IAopLogService<T> aopLogService;
    private final Class<T> tClass;
    private String TYPE_SUCCESS = "业务成功";
    private String TYPE_FAIL = "业务失败";
    private String TYPE_ASSERT = "错误";
    private String TYPE_ERROR = "异常";
    private boolean saveAssertLog = true;
    private Class<?> responseClass = null;
    private MultiValueMap<String, Object> successCondition = null;
    private Class<? extends RuntimeException> assertExceptionClass = null;

    private SaveEndpointEnum endpoint = SaveEndpointEnum.DB;

    private String[] headerNames = {"token", "content-type"};

    public SystemLogAspect(IAopLogService<T> aopLogService, Class<T> tClass, String... headerNames) {
        this.aopLogService = aopLogService;
        this.tClass = tClass;
        if (headerNames.length > 0) {
            this.headerNames = headerNames;
        }
    }

    public SystemLogAspect<T> setSuccessType(String s) {
        this.TYPE_SUCCESS = s;
        return this;
    }

    public SystemLogAspect<T> setFailType(String s) {
        this.TYPE_FAIL = s;
        return this;
    }

    public SystemLogAspect<T> setAssertType(String s) {
        this.TYPE_ASSERT = s;
        return this;
    }

    public SystemLogAspect<T> setErrorType(String s) {
        this.TYPE_ERROR = s;
        return this;
    }

    public SystemLogAspect<T> setSaveAssertLog(boolean b) {
        this.saveAssertLog = b;
        return this;
    }

    public SystemLogAspect<T> setAssertExceptionClass(Class<? extends RuntimeException> exceptionClass) {
        this.assertExceptionClass = exceptionClass;
        return this;
    }

    public SystemLogAspect<T> saveToES() {
        this.endpoint = SaveEndpointEnum.ES;
        return this;
    }

    public SaveEndpointEnum getSaveEndpoint() {
        return this.endpoint;
    }

    /**
     * 设置接口统一返回类，以及业务成功的条件，用于自动判断是否业务成功，不设置的情况，全部归为业务成功
     *
     * @param aClass 响应类
     * @param field  成功/失败 状态属性
     * @param value  成功值
     * @return this
     */
    public SystemLogAspect<T> setResponseClass(Class<?> aClass, String field, Object... value) {
        this.responseClass = aClass;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Object o : value) {
            map.add(field, o);
        }
        this.successCondition = map;
        return this;
    }


    @Pointcut("@annotation(com.github.leoiutil.core.annotation.AopLog)")
    public void controllerAspect() {
    }

    @Around("controllerAspect()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) {
        AopLog aopLog = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getAnnotation(AopLog.class);
        boolean value = aopLog.value();
        try {
            Object result = proceedingJoinPoint.proceed();
            // 保存日志 but it will not be saved if the value is false
            saveNormalLog(proceedingJoinPoint, result, value);
            return result;
        } catch (Throwable throwable) {
            // 如果出现异常，会记录异常信息
            saveExceptionLog(proceedingJoinPoint, throwable);
            //记录异常后将异常再次抛出到 全局异常处理器处理。
            throw new RuntimeException(throwable);
        }
    }

    private T createLogBase(ProceedingJoinPoint proceedingJoinPoint) {
        T instance;
        try {
            instance = tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        //获取请求相关参数
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String path = request.getRequestURI();
            String method = request.getMethod();
            Map<String, String> header = new HashMap<>();
            for (String name : headerNames) {
                header.put(name, request.getHeader(name));
            }
            String ip = IpUtils.getIp(request);
            instance.setPath(path);
            instance.setMethod(method);
            instance.setIp(ip);
            instance.setHeader(JSONUtils.toJSONString(header));
        }

        //获取用户相关参数
        SecurityContext context = SecurityContextHolder.getContext();
        String userId = null;
        String username = null;
        if (context.getAuthentication() != null) {
            ILogAuthAccount account = (ILogAuthAccount) context.getAuthentication().getPrincipal();
            userId = account.getAccountId();
            username = account.getAccountName();
        }

        //获取注解上参数
        AopLog aopLog = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getAnnotation(AopLog.class);
        String logName = aopLog.apiName();
        String logPlatform = aopLog.appPlatform();

        //获取方法中的参数
        Object[] args = proceedingJoinPoint.getArgs();
        String params = "{}";
        for (Object arg : args) {
            if ((arg instanceof ILogAuthAccount) || (arg instanceof Authentication)) {
                continue;
            }
            try {
                params = JSONUtils.toJSONString(arg);
            } catch (Exception ignored) {
            }

        }
        instance.setName(logName);
        instance.setPlatform(logPlatform);
        instance.setUserId(userId);
        instance.setUsername(username);
        instance.setParam(params);
        return instance;
    }

    private void saveExceptionLog(ProceedingJoinPoint proceedingJoinPoint, Throwable throwable) {
        Class<? extends Throwable> aClass = throwable.getClass();
        if (assertExceptionClass.equals(aClass)) {
            saveAssertLog(proceedingJoinPoint, throwable);
            return;
        }
        T t = createLogBase(proceedingJoinPoint);
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        t.setException(writer.toString());
        t.setType(TYPE_ERROR);
        unionSave(t);
    }

    private void saveNormalLog(ProceedingJoinPoint proceedingJoinPoint, Object result, boolean saveLog) {
        if (!saveLog) {
            return;
        }
        T t = createLogBase(proceedingJoinPoint);
        String string = JSONUtils.toJSONString(result);
        t.setResult(string);
        t.setType("");
        // 如果设置了 响应类以及成功条件
        if (result != null && result.getClass().equals(responseClass) && successCondition != null) {
            Set<String> strings = successCondition.keySet();
            String key = strings.stream().findFirst().orElse(null);
            if (key == null) {
                unionSave(t);
                return;
            }
            List<Object> list = successCondition.get(key);
            LinkedHashMap<?, ?> object = JSONUtils.parseObject(string);
            Object o = object.get(key);
            boolean flag = list.stream().anyMatch(o1 -> o1.equals(o));
            t.setType(flag ? TYPE_SUCCESS : TYPE_FAIL);
        }
        unionSave(t);
    }

    /**
     * assert 断言失败的记录，不归为异常
     *
     * @param proceedingJoinPoint proceedingJoinPoint
     * @param throwable           throwable
     */
    private void saveAssertLog(ProceedingJoinPoint proceedingJoinPoint, Throwable throwable) {
        if (!this.saveAssertLog) {
            return;
        }
        T t = createLogBase(proceedingJoinPoint);
        String message = throwable.getLocalizedMessage();
        t.setResult(JSONUtils.toJSONString(message));
        t.setType(TYPE_ASSERT);
        unionSave(t);
    }

    /**
     * 保存数据方法 根据设置 保存到数据库或者elasticsearch
     *
     * @param log 日志数据
     */
    private void unionSave(T log) {
        new Thread(() -> {
            switch (endpoint) {
                case ES:
                    break;
                case DB:
                    aopLogService.saveLog(log);
                    break;
                default:
                    break;
            }
        }).start();
    }
}
