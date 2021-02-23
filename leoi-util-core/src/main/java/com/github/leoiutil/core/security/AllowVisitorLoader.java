package com.github.leoiutil.core.security;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Li Yao Bing
 **/

@Component
public class AllowVisitorLoader {
    @Resource
    private AnnotationUtils annotationUtils;

    @SneakyThrows
    public AllowVisitorLoader(String classPath) {
        this.annotationUtils.getAllowVisitorUrl(classPath);
    }
}
