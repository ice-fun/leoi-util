package com.github.leoiutil.core.aop.interfaces;

/**
 * aopLog收集的时候，从实现此接口的账号类中获取需要的数据
 * 并且从params中剔除
 */
public interface ILogAuthAccount {

    String getAccountId();

    String getAccountName();
}
