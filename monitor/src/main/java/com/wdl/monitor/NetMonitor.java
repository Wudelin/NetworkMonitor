package com.wdl.monitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by: wdl at 2019/12/2 10:13
 * 标记注解
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NetMonitor
{
    NetState[] stateValue() default {NetState.GPRS, NetState.WIFI, NetState.NONE};
}
