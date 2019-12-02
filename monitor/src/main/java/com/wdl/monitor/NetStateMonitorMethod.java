package com.wdl.monitor;

import java.lang.reflect.Method;

/**
 * Create by: wdl at 2019/12/2 10:21
 * 订阅信息管理类
 */
public class NetStateMonitorMethod
{
    private Object object;
    private Method method;
    private NetState[] state = {NetState.WIFI, NetState.GPRS, NetState.NONE};

    public Object getObject()
    {
        return object;
    }

    public void setObject(Object object)
    {
        this.object = object;
    }

    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public NetState[] getState()
    {
        return state;
    }

    public void setState(NetState[] state)
    {
        this.state = state;
    }
}
