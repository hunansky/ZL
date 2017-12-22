package com.wdtx.littlebell.util;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Created by wending on 2017/6/27.
 */

public interface VolleyCallBack<T> {

    void onSuccess(T data);

    void onFail(int code, String msg);

    void onError(VolleyError error);
}
