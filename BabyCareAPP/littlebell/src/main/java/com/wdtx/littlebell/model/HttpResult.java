package com.wdtx.littlebell.model;

import java.util.ArrayList;

/**
 * Created by wending on 2017/6/27.
 */

public class HttpResult {
    private int code;
    private String message;
    private Object content;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
