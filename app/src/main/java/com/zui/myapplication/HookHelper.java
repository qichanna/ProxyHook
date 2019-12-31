package com.zui.myapplication;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HookHelper {
    public static final String TAG = "LocationHook";

    private static final Set<Object> hooked = new HashSet<>();

    public static void hookSystemServiceRegistry(){
        try {
            Object systemServiceFetchers  = null;
            Class<?> locationManagerClazsz = Class.forName("android.app.SystemServiceRegistry");
            //获取SystemServiceRegistry的SYSTEM_SERVICE_FETCHERS成员
            systemServiceFetchers = getField(locationManagerClazsz, null, "SYSTEM_SERVICE_FETCHERS");
            if(systemServiceFetchers instanceof HashMap){
                HashMap fetchersMap = (HashMap) systemServiceFetchers;
                Object locationServiceFetcher = fetchersMap.get(Context.LOCATION_SERVICE);
                Class<?> serviceFetcheClazz = Class.forName("android.app.SystemServiceRegistry$ServiceFetcher");
                //创建代理类
                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class<?>[] { serviceFetcheClazz }, new LMCachedServiceFetcherProxy(locationServiceFetcher));
                //用代理类替换掉原来的ServiceFetcher
                if(fetchersMap.put(Context.LOCATION_SERVICE, proxy) == locationServiceFetcher){
                    Log.d("LocationTest", "hook success! ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookLocationManager(LocationManager locationManager) {
        try {
            Object iLocationManager = null;
            Class<?> locationManagerClazsz = Class.forName("android.location.LocationManager");
            //获取LocationManager的mService成员
            iLocationManager = getField(locationManagerClazsz, locationManager, "mService");

            if(hooked.contains(iLocationManager)){
                return;//这个实例已经hook过啦
            }

            Class<?> iLocationManagerClazz = Class.forName("android.location.ILocationManager");

            //创建代理类
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iLocationManagerClazz}, new ILocationManagerProxy(iLocationManager));

            //在这里移花接木，用代理类替换掉原始的ILocationManager
            setField(locationManagerClazsz, locationManager, "mService", proxy);
            //记录已经hook过的实例
            hooked.add(proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getField(Class clazz, Object target, String name) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    public static void setField(Class clazz, Object target, String name, Object value) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
