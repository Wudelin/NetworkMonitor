package com.wdl.networkmonitor;

import android.app.Application;

import com.wdl.monitor.NetStateMonitor;

/**
 * Create by: wdl at 2019/12/2 16:03
 */
public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        NetStateMonitor.getInstance().init(this);
    }
}
