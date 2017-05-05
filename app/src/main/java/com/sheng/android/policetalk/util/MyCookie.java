package com.sheng.android.policetalk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by Administrator on 2017/2/21.
 */

public class MyCookie implements CookieJar {
    private String host;
    public MyCookie(String host){
        this.host=host;
    }
    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
    private Cookie cookie;
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        System.out.println(url.toString());
//        cookieStore.put(HttpUrl.parse(host), cookies);
//        System.out.println("------------------------保存cookie-------------------------------");
        for(Cookie cookie:cookies){
            System.out.println("cookie Name:"+cookie.name());
            if(cookie.name().equals("JSESSIONID")){
                this.cookie=cookie;
            }
//            System.out.println("cookie Path:"+cookie.path());
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies=new ArrayList<>();
        if(cookie!=null)
        cookies.add(cookie);
        return cookies;
//        List<Cookie> cookies = cookieStore.get(HttpUrl.parse(host));
//        if(cookies==null){
//            System.out.println("没加载到cookie");
//        }
//        return cookies != null ? cookies : new ArrayList<Cookie>();
    }
    public void clearCookie(){
        cookieStore.clear();
    }
}
