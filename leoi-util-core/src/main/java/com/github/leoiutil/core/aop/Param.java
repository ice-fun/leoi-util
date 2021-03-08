package com.github.leoiutil.core.aop;

import lombok.Data;

/**
 * @author Li Yao Bing
 **/

@Data
public class Param {

    private String path;
    private String method;
    private String type;
    private String name;
    private String platform;
    private String ip;
    private String userId;
    private String username;
    private String header;
    private String param;
    private String result;
    private Integer fixedStatus;

    private String keyword;
    private Long pageNo;
    private Long pageSize;
    private String fromTime;
    private String toTime;
}
