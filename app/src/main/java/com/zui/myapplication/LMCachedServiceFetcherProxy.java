package com.zui.myapplication;

import android.location.LocationManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LMCachedServiceFetcherProxy implements InvocationHandler {
    private Object mLMCachedServiceFetcher;

    public LMCachedServiceFetcherProxy(Object LMCachedServiceFetcher) {
        this.mLMCachedServiceFetcher = LMCachedServiceFetcher;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //为什么拦截getService，而不是createService?
        if(TextUtils.equals(method.getName(), "getService")){
            Object result = method.invoke(mLMCachedServiceFetcher, args);
            if(result instanceof LocationManager){
                //在这里hook LocationManager
                HookHelper.hookLocationManager((LocationManager)result);
            }
            return result;
        }
        return method.invoke(mLMCachedServiceFetcher, args);
    }
}
