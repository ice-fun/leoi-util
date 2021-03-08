package com.github.leoiutil.core.security;

import com.github.leoiutil.core.annotation.AllowVisitor;
import com.github.leoiutil.core.spring.ApplicationContextUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author lyb
 * 需要注入 {@link com.github.leoiutil.core.spring.ApplicationContextUtils} bean
 **/

public class AnnotationUtil {

    public static List<AllowVisitorUtils.VisitorRequest> getAllowVisitorUrl() {
        List<AllowVisitorUtils.VisitorRequest> list = new ArrayList<>();

        Map<String, Object> map = ApplicationContextUtils.getBeansWithAnnotation(RequestMapping.class);
        Collection<Object> objects = map.values();
        for (Object o : objects) {
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(o.getClass(), RequestMapping.class);
            assert requestMapping != null;
            String[] paths = requestMapping.value();
            Method[] methods = o.getClass().getMethods();
            for (Method method : methods) {
                AllowVisitor visitor = AnnotationUtils.findAnnotation(method, AllowVisitor.class);
                if (visitor == null) {
                    continue;
                }
                String[] value = getPathFromMethod(method);
                if (value == null) {
                    continue;
                }
                boolean checkToken = visitor.authentication();
                for (String path : paths) {
                    for (String s : value) {
                        String url = path + s;
                        String finalURL = url.replaceAll("//", "/");
                        AllowVisitorUtils.VisitorRequest visitorRequest = new AllowVisitorUtils.VisitorRequest();
                        visitorRequest.setPath(new AntPathRequestMatcher(finalURL));
                        visitorRequest.setAuthentication(checkToken);
                        list.add(visitorRequest);
                    }
                }
            }
        }
        return list;
    }

    private static String[] getPathFromMethod(Method method) {
        RequestMapping annotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (annotation != null && annotation.value().length > 0) {
            return annotation.value();
        }
        GetMapping annotation1 = AnnotationUtils.findAnnotation(method, GetMapping.class);
        if (annotation1 != null && annotation1.value().length > 0) {
            return annotation1.value();
        }
        PostMapping annotation2 = AnnotationUtils.findAnnotation(method, PostMapping.class);
        if (annotation2 != null && annotation2.value().length > 0) {
            return annotation2.value();
        }
        DeleteMapping annotation3 = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
        if (annotation3 != null && annotation3.value().length > 0) {
            return annotation3.value();
        }
        PutMapping annotation4 = AnnotationUtils.findAnnotation(method, PutMapping.class);
        if (annotation4 != null && annotation4.value().length > 0) {
            return annotation4.value();
        }
        return null;
    }

}
