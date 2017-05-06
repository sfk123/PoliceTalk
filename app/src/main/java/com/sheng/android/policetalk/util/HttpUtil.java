package com.sheng.android.policetalk.util;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.ReturnData;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/14.
 */

public class HttpUtil {
    private OkHttpClient httpClient;
    private static HttpUtil instance=null;
    private String tag="okhttp:";
    private MyCookie cookieJar;
    public void clearCookie(){
        cookieJar.clearCookie();
    }
    private  HttpUtil(){
        cookieJar=new MyCookie(URLConfig.getHostForCookie());
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .cookieJar(cookieJar);
        httpClient=builder.build();
    }
    public static HttpUtil getInstance(){
        if(instance==null){
            instance=new HttpUtil();
        }
        return instance;
    }
    public void postAsynHttp(String url, Map<String,String> parameters, final HttpCallBack callBack){
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String,String> entry : parameters.entrySet()) {
            builder.add(entry.getKey(),entry.getValue());
        }
        final Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        Log.i(tag,request.url().toString());
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            int serversLoadTimes = 0;
            int maxLoadTimes=3;
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(e instanceof SocketTimeoutException && serversLoadTimes<maxLoadTimes)//如果超时并未超过指定次数，则重新连接
                {
                    System.out.println("重连");
                    serversLoadTimes++;
                    httpClient.newCall(call.request()).enqueue(this);
                }else {
                    e.printStackTrace();
                    callBack.onFailure("连接超时，请检查网络后重试");
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(tag, str);
                try{
                    ReturnData data= JSON.parseObject(str,ReturnData.class);
                    if(callBack!=null)
                    callBack.onSuccess(data);
                }catch (Exception e){
                    e.printStackTrace();
                    if(callBack!=null)
                    callBack.onFailure("JSON转换失败："+e.getMessage());
                }
            }

        });
    }
    public void postAsynHttp(String url, Map<String,String> parameters, File file, final HttpCallBack callBack){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(file != null){
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("arm/*"), file);
            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("voice", file.getName(), body);
        }
        if (parameters != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry<String,String> entry : parameters.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody.build())
                .build();
        Log.i(tag,request.url().toString());
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(tag,e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(tag, str);
                try{
                    ReturnData data= JSON.parseObject(str,ReturnData.class);
                    if(callBack!=null)
                        callBack.onSuccess(data);
                }catch (Exception e){
                    e.printStackTrace();
                    if(callBack!=null)
                        callBack.onFailure("JSON转换失败："+e.getMessage());
                }
            }

        });
    }
    public void UploadPhoto(String url, Map<String,String> parameters, File file, final HttpCallBack callBack){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(file != null){
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("photo", file.getName(), body);
        }
        if (parameters != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry<String,String> entry : parameters.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody.build())
                .build();
        Log.i(tag,request.url().toString());
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(tag,e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(tag, str);
                try{
                    ReturnData data= JSON.parseObject(str,ReturnData.class);
                    if(callBack!=null)
                        callBack.onSuccess(data);
                }catch (Exception e){
                    e.printStackTrace();
                    if(callBack!=null)
                        callBack.onFailure("JSON转换失败："+e.getMessage());
                }
            }

        });
    }
}
