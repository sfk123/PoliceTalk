package com.sheng.android.policetalk.modal;

/**
 * Created by Administrator on 2017/2/13.
 */
public class ReturnData {
    public boolean status;
    public Object data;
    public String message;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
