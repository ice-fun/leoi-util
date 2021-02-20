package com.github.leoiutil.core.security;

import com.github.leoiutil.core.annotation.AllowVisitor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Li Yao Bing
 * @Company https://www.knowswift.com/
 * @Date 2021/2/20
 **/


@Component
public class AnnotationUtil {

    @Resource
    private ResourceLoader resourceLoader;

    private static final String VALUE = "value";

    public AllowVisitorRequest getAllowVisitorUrl(String classPath) throws Exception {

        List<AllowVisitorRequest.VisitorRequest> list = new ArrayList<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        org.springframework.core.io.Resource[] resources = resolver.getResources(classPath);
        for (org.springframework.core.io.Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            resolveClass(reader, list, AllowVisitor.class);
        }
        return new AllowVisitorRequest(list);
    }

    private <T> void resolveClass(MetadataReader reader, List<AllowVisitorRequest.VisitorRequest> list, Class<T> tagAnnotationClass) {
        String tagAnnotationClassCanonicalName = tagAnnotationClass.getCanonicalName();
        //获取注解元数据
        AnnotationMetadata annotationMetadata = reader.getAnnotationMetadata();
        //获取类中RequestMapping注解的属性
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(RequestMapping.class.getCanonicalName());

        //若类无RequestMapping注解
        if (annotationAttributes == null) {
            return;
        }
        //获取RequestMapping注解的value
        String[] pathParents = (String[]) annotationAttributes.get(VALUE);
        if (0 == pathParents.length) {
            return;
        }

        //获取RequestMapping注解的value
        String pathParent = pathParents[0];

        //获取当前类中已添加要扫描注解的方法
        Set<MethodMetadata> annotatedMethods = annotationMetadata.getAnnotatedMethods(tagAnnotationClassCanonicalName);

        for (MethodMetadata annotatedMethod : annotatedMethods) {
            //获取当前方法中要扫描注解的属性
            Map<String, Object> targetAttr = annotatedMethod.getAnnotationAttributes(tagAnnotationClassCanonicalName);
            //获取当前方法中要xxxMapping注解的属性
            Map<String, Object> mappingAttr = getPathByMethod(annotatedMethod);
            if (mappingAttr == null) {
                continue;
            }

            String[] childPath = (String[]) mappingAttr.get(VALUE);
            if (targetAttr == null || childPath == null || childPath.length == 0) {
                continue;
            }
            Boolean token = (Boolean) targetAttr.get("authentication");
            boolean checkToken = token == null || token;
            String path = pathParent + childPath[0];
            String replace = path.replace("//", "/");
            AllowVisitorRequest.VisitorRequest visitorRequest = new AllowVisitorRequest.VisitorRequest();
            visitorRequest.setPath(new AntPathRequestMatcher(replace));
            visitorRequest.setCheckToken(checkToken);
            list.add(visitorRequest);
        }
    }


    private Map<String, Object> getPathByMethod(MethodMetadata annotatedMethod) {
        Map<String, Object> attributes = annotatedMethod.getAnnotationAttributes(GetMapping.class.getCanonicalName());
        if (attributes != null && attributes.get(VALUE) != null) {
            return attributes;
        }
        attributes = annotatedMethod.getAnnotationAttributes(PostMapping.class.getCanonicalName());
        if (attributes != null && attributes.get(VALUE) != null) {
            return attributes;
        }

        attributes = annotatedMethod.getAnnotationAttributes(DeleteMapping.class.getCanonicalName());
        if (attributes != null && attributes.get(VALUE) != null) {
            return attributes;
        }

        attributes = annotatedMethod.getAnnotationAttributes(PutMapping.class.getCanonicalName());
        if (attributes != null && attributes.get(VALUE) != null) {
            return attributes;
        }
        attributes = annotatedMethod.getAnnotationAttributes(RequestMapping.class.getCanonicalName());
        return attributes;
    }

}
