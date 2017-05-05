package com.sheng.android.policetalk.util;

import com.sheng.android.policetalk.modal.ReturnData;

/**
 * Created by Administrator on 2017/2/14.
 */

public interface HttpCallBack {
    void onSuccess(ReturnData responseBody);
    void onFailure(String message);
}
