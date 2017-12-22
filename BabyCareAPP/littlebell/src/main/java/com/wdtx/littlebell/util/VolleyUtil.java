package com.wdtx.littlebell.util;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.model.HttpResult;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by wending on 2017/6/27.
 */

public class VolleyUtil {

    private static VolleyUtil mVolleyUtil;

    //volley请求队列
    private static RequestQueue mRequestQueue;

    private Gson mGson;

    private static final String BASE_URL = "http://192.168.191.1/api/v1";
    //连接超时时间
    private static final int REQUEST_TIMEOUT_TIME = 60 * 1000;

    private static final int SUCCESS_CODE = 100;

    public VolleyUtil() {
        mGson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .setDateFormat(DateFormat.LONG)
                .create();
        mRequestQueue = Volley.newRequestQueue(MyApplication.instance);
    }

    public static VolleyUtil getInstance() {
        if (mVolleyUtil == null) {
            synchronized (VolleyUtil.class) {
                if (mVolleyUtil == null) {
                    mVolleyUtil = new VolleyUtil();
                }
            }
        }
        return mVolleyUtil;
    }

    private <T> void request(String url, int method, final Map<String, String> param,
                             final VolleyCallBack<T> volleyCallBack) {
        Listener<T> listener = new Listener<>(volleyCallBack);
        StringRequest stringRequest = new StringRequest(method, BASE_URL + url,
                listener, listener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //请求参数
                return param;
            }
        };

    }

    public <T> void requestGet(String url, final Map<String, String> param,
                               VolleyCallBack<T> volleyCallBack) {
        Listener<T> listener = new Listener<>(volleyCallBack);
        StringBuilder sb = new StringBuilder(BASE_URL + url);
        if (param != null && param.size() > 0) {
            sb.append("?");
            for (Map.Entry<String, String> entry : param.entrySet()) {
                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            Log.i("wending", "requestGet: " + sb.toString());
        }

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,sb.toString(),listener,listener);
        addToQueue(stringRequest,url);
    }

    public <T> void requestPost(String url, final Map<String, String> param,
                                VolleyCallBack<T> volleyCallBack) {
        Listener<T> listener = new Listener<>(volleyCallBack);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,BASE_URL + url,listener,listener){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return param;
            }
        };
        addToQueue(stringRequest,url);
    }

    public static RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void cancel(String url) {
        mRequestQueue.cancelAll(url);
    }

    private class Listener<T> implements Response.Listener<String>, Response.ErrorListener {

        VolleyCallBack<T> volleyCallBack;

        public Listener(VolleyCallBack<T> volleyCallBack) {
            this.volleyCallBack = volleyCallBack;
        }

        @Override
        public void onResponse(String s) {
            if (volleyCallBack == null) {
                return;
            }

            Type type = getTType(volleyCallBack.getClass());

            HttpResult httpResult = mGson.fromJson(s, HttpResult.class);
            if (httpResult != null) {
                //失败
                if (httpResult.getCode() != SUCCESS_CODE) {
                    volleyCallBack.onFail(
                            httpResult.getCode(),
                            httpResult.getMessage());
                } else {//成功
                    //获取data对应的json字符串
                    String json = mGson.toJson(httpResult.getContent());
                    //泛型是String，返回结果json字符串
                    if (type == String.class) {
                        volleyCallBack.onSuccess((T) json);
                        //泛型是实体或者List<>
                    } else {
                        T t = mGson.fromJson(json, type);
                        volleyCallBack.onSuccess(t);
                    }
                }
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (volleyCallBack == null) {
                return;
            }
            volleyCallBack.onError(volleyError);
        }
    }

    private Type getTType(Class<?> clazz) {
        Type mySuperClassType = null;
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                if (paramType.getRawType().equals(VolleyCallBack.class)) {
                    Type[] types = paramType.getActualTypeArguments();
                    if (types != null && types.length > 0) {
                        return types[0];
                    }
                }
            }
        }
        return null;
    }

    private void addToQueue(StringRequest stringRequest, String url) {
        //设置请求超时和重试
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT_TIME, 1, 1.0f));
        //加入到请求队列
        if (mRequestQueue != null) {
            stringRequest.setTag(url);
            mRequestQueue.add(stringRequest);
        }
    }

    public static DefaultCallBack defaultCallBack = new DefaultCallBack();

    public static class DefaultCallBack implements VolleyCallBack<String>{

        @Override
        public void onSuccess(String data) {
            Log.i("wending", "onSuccess: " + data);
        }

        @Override
        public void onFail(int code, String msg) {
            Log.i("wending", "onFail: " + code + "->" + msg);
        }

        @Override
        public void onError(VolleyError error) {
            Log.i("wending", "onError: " + error.getMessage());
        }
    }


}
