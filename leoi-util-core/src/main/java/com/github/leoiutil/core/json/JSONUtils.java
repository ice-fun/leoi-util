package com.github.leoiutil.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Li Yao Bing
 * <p>
 * 此工具是对Jackson库API的简单封装
 * 由于Fastjson漏洞过多，此后Fastjson将会逐步去除，改用此工具类的方法
 * ObjectMapper的自定义配置参考Jackson官方，方法根据需求封装
 **/

public class JSONUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    static {

        //设置java.util.Date时间类的序列化以及反序列化的格式
        objectMapper.setDateFormat(new SimpleDateFormat(DATETIME_PATTERN));

        // 初始化JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //处理LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        //处理LocalDate
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        //处理LocalTime
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

        //注册时间模块, 支持支持jsr310, 即新的时间类(java.time包下的时间类)
        objectMapper.registerModule(javaTimeModule);

        // 包含所有字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // 在序列化一个空对象时不抛出异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 忽略反序列化时在json字符串中存在, 但在java对象中不存在的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Java类序列化 为JSON 字符串
     *
     * @param object 类
     * @param <T>    泛型
     * @return 序列化结果
     */
    @SneakyThrows
    public static <T> String toJSONString(T object) {
        if (object == null) {
            return null;
        }
        return object instanceof String ? (String) object : objectMapper.writeValueAsString(object);
    }

    /**
     * JSON字符串反序列化为Java类
     * Jackson的JSON数据结构用LinkedHashMap实现，因此 但不指定Java对象的时候，
     * 反序列化为默认的LinkedHashMap
     *
     * @param string JSON字符串
     * @return JSON对象（LinkedHashMap）
     */
    @SneakyThrows
    public static LinkedHashMap<?, ?> parseObject(String string) {
        return objectMapper.readValue(string, LinkedHashMap.class);
    }

    /**
     * 重载 parseObject
     * 将JSON字符串序列化为指定的Java类
     *
     * @param string JSON字符串
     * @param <T>    泛型
     * @param clazz  JavaType
     * @return JavaBean
     */
    @SneakyThrows
    public static <T> T parseObject(String string, Class<T> clazz) {
        return objectMapper.readValue(string, clazz);
    }

    /**
     * 将JSON List 字符串序列化为指定的Java类
     *
     * @param string JSON字符串
     * @param <T>    泛型
     * @param clazz  JavaType
     * @return JavaBean list
     */
    @SneakyThrows
    public static <T> List<T> parseArray(String string, Class<T> clazz) {
        return objectMapper.readValue(string, getCollectionType(ArrayList.class, clazz));
    }

    /**
     * 重载 parseArray
     * 从数据库查询出的JSON 数组类型数据，JavaType 是 map结构，
     * 通过此方法转为 JavaType list
     *
     * @param list  JSON List
     * @param <T>   泛型
     * @param clazz JavaType
     * @return JavaBean list
     */
    @SneakyThrows
    public static <T> List<T> parseArray(List<?> list, Class<T> clazz) {

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(o -> parseObject(toJSONString(o), clazz)).collect(Collectors.toCollection(() -> new ArrayList<>(list.size())));
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
