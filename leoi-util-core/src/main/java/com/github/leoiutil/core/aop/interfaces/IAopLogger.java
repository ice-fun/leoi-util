package com.github.leoiutil.core.aop.interfaces;

/**
 * @author Li Yao Bing
 **/


public interface IAopLogger {

    /**
     * @param path 请求路径
     */
    void setPath(String path);

    /**
     *
     * @param method 请求方法 POST/GET/PUT ...
     */
    void setMethod(String method);

    void setType(String type);

    void setName(String name);

    void setPlatform(String platform);

    void setIp(String ip);

    void setUserId(String userId);

    void setUsername(String username);

    void setHeader(String header);

    void setParam(String param);

    void setResult(String result);

    void setException(String exception);

    void setFixedStatus(Integer exception);
}
