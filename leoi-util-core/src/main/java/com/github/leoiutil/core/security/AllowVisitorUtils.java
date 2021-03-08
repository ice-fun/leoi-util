package com.github.leoiutil.core.security;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lyb
 *
 * 使用这个工具类，需要在spring配置文件 注入
 * 同时需要注入 {@link com.github.leoiutil.core.spring.ApplicationContextUtils} bean
 *
 **/

public class AllowVisitorUtils {

    @Getter
    private final List<VisitorRequest> matchers;

    public AllowVisitorUtils() {
        this.matchers = AnnotationUtil.getAllowVisitorUrl();
    }

    @Data
    public static class VisitorRequest {
        private RequestMatcher path;
        private boolean authentication;
    }

    public boolean anyPermission(HttpServletRequest request) {
        return this.matchers.stream().filter(matcher -> !matcher.isAuthentication()).anyMatch(matcher -> matcher.path.matches(request));
    }

    public boolean visitorPermission(HttpServletRequest request) {
        return this.matchers.stream().filter(VisitorRequest::isAuthentication).anyMatch(matcher -> matcher.path.matches(request));
    }
}
