package com.wdl.monitor;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by: wdl at 2019/12/2 10:17
 * 与EventBus相似
 * 网络状态监听管理类：
 * 对外提供注册、反注册api
 */
@SuppressWarnings("unused")
public class NetStateMonitor
{
    private static volatile NetStateMonitor defaultInstance;
    /**
     * Application 用来注册广播等
     */
    private Application mApplication;
    private static final String NET_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * 存储每个Class中对应需要根据网络状态来执行的方法的信息类
     */
    private Map<Object, List<NetStateMonitorMethod>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 获取单例 双重检查
     *
     * @return NetStateMonitor
     */
    public static NetStateMonitor getInstance()
    {
        NetStateMonitor instance = defaultInstance;
        if (instance == null)
        {
            synchronized (NetStateMonitor.class)
            {
                instance = NetStateMonitor.defaultInstance;
                if (instance == null)
                {
                    instance = NetStateMonitor.defaultInstance = new NetStateMonitor();
                }
            }
        }
        return instance;
    }

    private NetStateMonitor()
    {
    }

    /**
     * 初始化 注册监听等
     *
     * @param application Application
     */
    public void init(Application application)
    {
        if (application == null)
        {
            throw new NullPointerException("application can not null...");
        }
        this.mApplication = application;
        // 根据不同API等级 注册网络状态变化的广播，回调等
        registerMonitor();
    }

    /**
     * 根据不同API等级 注册网络状态变化的广播，回调等
     */
    private void registerMonitor()
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                this.mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
        {
            throw new NullPointerException("获取ConnectivityManager失败，请检查权限");
        }
        // api 大于等于 26时 => ConnectivityManager.registerDefaultNetworkCallback
        final int api = Build.VERSION.SDK_INT;
        if (api >= Build.VERSION_CODES.O)
        {
            connectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
        } else if (api >= Build.VERSION_CODES.LOLLIPOP) // api大于等于21时，新增的API
        {
            final NetworkRequest.Builder builder = new NetworkRequest.Builder();
            final NetworkRequest request = builder.build();
            connectivityManager.registerNetworkCallback(request, mNetworkCallback);
        } else // 小于21,动态注册广播
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NET_ACTION);
            this.mApplication.registerReceiver(receiver, filter);
        }
    }

    /**
     * 注册监听
     *
     * @param object Object
     */
    public void register(@NonNull Object object)
    {
        final List<NetStateMonitorMethod> monitorMethodList = find(object);
    }

    /**
     * 缓存获取：
     * 1.不存在—>反射查找->推入缓存
     *
     * @param object Object
     * @return List<NetStateMonitorMethod>
     */
    private List<NetStateMonitorMethod> find(Object object)
    {
        List<NetStateMonitorMethod> monitorMethodList = cacheMap.get(object);
        if (monitorMethodList != null)
        {
            return monitorMethodList;
        }

        // 反射查找
        monitorMethodList = useReflectFind(object);

        if (monitorMethodList.isEmpty())
        {
            throw new NullPointerException("classes have no public methods with the @NetMonitor annotation");
        } else
        {
            cacheMap.put(object, monitorMethodList);
            return monitorMethodList;
        }
    }

    /**
     * 通过反射查找注解方法
     *
     * @param object Object
     * @return List<NetStateMonitorMethod>
     */
    private List<NetStateMonitorMethod> useReflectFind(Object object)
    {
        final List<NetStateMonitorMethod> monitorMethods = new ArrayList<>();
        final Class clazz = object.getClass();
        Method[] methods;
        try
        {
            methods = clazz.getDeclaredMethods();
        } catch (Throwable e)
        {
            methods = clazz.getMethods();
        }
        // 遍历方法
        for (Method method : methods)
        {
            int modifier = method.getModifiers();
            // 为Public修饰
            if ((modifier & Modifier.PUBLIC) != 0)
            {
                Class<?>[] paramTypes = method.getParameterTypes();
                // 参数长度为1
                if (paramTypes.length == 1)
                {
                    // 参数类型为 NetState
                    if (!paramTypes[0].getName().equals(NetState.class.getName()))
                    {
                        continue;
                    }
                    NetMonitor annotation = method.getAnnotation(NetMonitor.class);
                    NetStateMonitorMethod stateMonitorMethod;
                    if (annotation != null)
                    {
                        stateMonitorMethod = new NetStateMonitorMethod();
                        stateMonitorMethod.setMethod(method);
                        stateMonitorMethod.setObject(object);
                        stateMonitorMethod.setState(annotation.stateValue());
                        monitorMethods.add(stateMonitorMethod);
                    }
                }
            }
        }
        return monitorMethods;

    }

    /**
     * 取消注册
     *
     * @param object Object
     */
    public void unregister(@NonNull Object object)
    {
        cacheMap.remove(object);
    }

    final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback()
    {
        /**
         * 网络可用
         * @param network Network
         */
        @Override
        public void onAvailable(@NonNull Network network)
        {
            super.onAvailable(network);
            post(getNetState());
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive)
        {
            super.onLosing(network, maxMsToLive);
        }

        /**
         * 不可用
         * @param network Network
         */
        @Override
        public void onLost(@NonNull Network network)
        {
            super.onLost(network);
            post(NetState.NONE);
        }

        @Override
        public void onUnavailable()
        {
            super.onUnavailable();
        }
    };

    private NetState getNetState()
    {
        NetState netState = NetState.NONE;
        int type = NetworkUtil.getNetType(mApplication);
        switch (type)
        {
            case 0:
                netState = NetState.NONE;
                break;
            case 1:
                netState = NetState.WIFI;
                break;
            case 2:
            case 3:
            case 4:
                netState = NetState.GPRS;
                break;
            default:
                break;
        }
        return netState;
    }

    private void post(NetState state)
    {
        final Set<Object> mSet = cacheMap.keySet();
        for (Object clazz : mSet)
        {
            List<NetStateMonitorMethod> monitorMethods = cacheMap.get(clazz);
            if (monitorMethods == null || monitorMethods.isEmpty())
            {
                continue;
            }
            for (NetStateMonitorMethod monitorMethod : monitorMethods)
            {
                invoke(monitorMethod, state);
            }
        }
    }

    /**
     * 执行指定网络状态的方法
     *
     * @param monitorMethod NetStateMonitorMethod
     * @param netState      NetState
     */
    private void invoke(NetStateMonitorMethod monitorMethod, NetState netState)
    {
        final NetState[] states = monitorMethod.getState();
        for (NetState state : states)
        {
            if (state == netState)
            {
                try
                {
                    monitorMethod.getMethod().invoke(monitorMethod.getObject(), state);
                    return;
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                } catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 广播
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            if (action.toUpperCase().equals(NET_ACTION))
            {
                post(getNetState());
            }
        }
    };
}
