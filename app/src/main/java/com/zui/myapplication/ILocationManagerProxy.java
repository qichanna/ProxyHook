package com.zui.myapplication;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ILocationManagerProxy implements InvocationHandler {
    private Object mLocationManager;

    public ILocationManagerProxy(Object locationManager) {
        this.mLocationManager = locationManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (TextUtils.equals("requestLocationUpdates", method.getName())) {
            //获取当前函数调用栈
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null || stackTrace.length < 3) {
                return null;
            }
            StackTraceElement log = stackTrace[2];
            String invoker = null;
            boolean foundLocationManager = false;
            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement e = stackTrace[i];
                if (TextUtils.equals(e.getClassName(), "android.location.LocationManager")) {
                    foundLocationManager = true;
                    continue;
                }
                //找到LocationManager外层的调用者
                if (foundLocationManager && !TextUtils.equals(e.getClassName(), "android.location.LocationManager")) {
                    invoker = e.getClassName() + "." + e.getMethodName();
                    //此处可将定位接口的调用者信息根据自己的需求进行记录，这里我将调用类、函数名、以及参数打印出来
                    Log.d("LocationTest", "invoker is " + invoker + "(" + args + ")");
                    break;
                }
            }
        }
        return method.invoke(mLocationManager, args);
    }
}
