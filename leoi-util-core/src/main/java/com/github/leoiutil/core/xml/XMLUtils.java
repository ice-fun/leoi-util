package com.github.leoiutil.core.xml;

import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class XMLUtils {

    /**
     * java对象转成xml文件
     *
     * @param obj      java对象
     * @param load     类对象
     * @param encoding 编码格式
     * @return xml字符串
     */
    @SneakyThrows
    public static String beanToXml(Object obj, Class<?> load, String encoding) {
        JAXBContext context = JAXBContext.newInstance(load);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        return writer.toString();
    }

    /**
     * xml文件转换成java对象
     *
     * @param xmlPath xml文件路径
     * @param tClass  java对象.class
     * @param <T>     泛型
     * @return T
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T xmlToBean(String xmlPath, Class<T> tClass) {
        JAXBContext context = JAXBContext.newInstance(tClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xmlPath));
    }

    /**
     * JavaBean转换成xml,默认编码UTF-8
     *
     * @param obj java对象
     * @return XML字符串
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /**
     * JavaBean转换成xml
     *
     * @param obj      java对象
     * @param encoding 编码格式
     * @return XML字符串
     */
    @SneakyThrows
    public static String convertToXml(Object obj, String encoding) {

        JAXBContext context = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        return writer.toString();

    }

    /**
     * JavaBean转换成xml去除xml声明部分
     *
     * @param obj      java对象
     * @param encoding 编码
     * @return XML字符串
     */
    @SneakyThrows
    public static String convertToXmlIgnoreXmlHeader(Object obj, String encoding) {

        JAXBContext context = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        return writer.toString();
    }

    /**
     * xml转换成JavaBean
     *
     * @param xml    xml字符串
     * @param tClass java类
     * @param <T>    泛型
     * @return javaBean
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T convertToJavaBean(String xml, Class<T> tClass) {
        JAXBContext context = JAXBContext.newInstance(tClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }

}
