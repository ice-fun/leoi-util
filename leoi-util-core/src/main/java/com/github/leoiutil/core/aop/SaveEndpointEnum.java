package com.github.leoiutil.core.aop;

import lombok.Getter;

/**
 * @author Li Yao Bing
 * 日志的保存终点 关系型数据库 或者 elasticsearch
 **/
@Getter
public enum SaveEndpointEnum {

    /**
     * 关系型数据库
     */
    DB,

    /**
     * elasticsearch
     */
    ES
}
