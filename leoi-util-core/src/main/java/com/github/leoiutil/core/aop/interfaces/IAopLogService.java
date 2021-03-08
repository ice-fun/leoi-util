package com.github.leoiutil.core.aop.interfaces;

import com.github.leoiutil.core.aop.Param;

import java.io.Serializable;

/**
 * @author Li Yao Bing
 **/

public interface IAopLogService<T extends IAopLogger> {
    boolean saveLog(T entity);

    T logDetails(Serializable id);

    /**
     * 查询功能
     *
     * @param param param
     * @return 实现了ILogPage 接口的分页类
     */
    default ILogPage<T> logPage(Param param) {
        return null;
    }

    /**
     * 分页查询功能
     *
     * @param param param
     * @return 需要强转为所需的类型
     */
    default Object logList(Param param) {
        return null;
    }

    boolean fixedException(T entity);
}
