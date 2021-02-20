package com.github.leoiutil.core.security;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author knowswift
 * @Company https://www.knowswift.com/
 * @Date 2020/12/31
 **/

@Getter
public class AllowVisitorRequest {
    private List<VisitorRequest> matchers;

    public AllowVisitorRequest() {
    }

    public AllowVisitorRequest(List<VisitorRequest> matchers) {
        this.matchers = matchers;
    }

    @Data
    public static class VisitorRequest {
        private RequestMatcher path;
        private boolean checkToken;
    }

    public boolean anyPermission(HttpServletRequest request){
        return this.matchers.stream().filter(matcher -> !matcher.isCheckToken()).anyMatch(matcher -> matcher.path.matches(request));
    }

    public boolean visitorPermission(HttpServletRequest request){
        return this.matchers.stream().filter(VisitorRequest::isCheckToken).anyMatch(matcher -> matcher.path.matches(request));
    }
}
