package com.sheng.android.policetalk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.view.LoadingDialog;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity implements HttpCallBack{

    @Bind(R.id.username)
    public EditText edt_username;
    @Bind(R.id.password)
    public EditText edt_password;
    @Bind(R.id.checkbox)
    public ImageView checkbox;
    @Bind(R.id.tv_remeber)
    TextView tv_remeber;
    @Bind(R.id.login)
    Button login;

    private HttpUtil httpUtil;
    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        httpUtil=HttpUtil.getInstance();
        EventBus.getDefault().register(this);
        EventModal eventModal=new EventModal();
        eventModal.setType("isLogin");
        EventBus.getDefault().post(eventModal, "isLogin");
        SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE);
        edt_username.setText(sharedPreferences.getString("username",""));
        edt_password.setText(sharedPreferences.getString("password",""));

        Locale myLocale;
        String language=sharedPreferences.getString("language","中文");
        if(language.equals("中文")){
            myLocale = new Locale("zh");
        }else{
            myLocale = new Locale("en");
        }
        res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        setString();
    }
    private void setString(){
        edt_username.setHint(res.getString(R.string.hint_username));
        edt_password.setHint(res.getString(R.string.hint_pwd));
        tv_remeber.setText(res.getString(R.string.tv_remeber));
        login.setText(res.getString(R.string.btn_login));
    }
    @Subscriber(tag = "isLogin_back")
    private void connectEvent(EventModal event) {//登录界面查看是否已经登录
        if(event.getType().equals("isLogin")){
            User user=(User)event.getData();
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("user",user);
            intent.putExtras(mBundle);
            startActivity(intent);
            finish();
        }
    }
    @OnClick(R.id.login)
    public void login(){
        String username=edt_username.getText().toString();
        String password=edt_password.getText().toString();
        if(username.equals("")){
            Toast.makeText(this,res.getString(R.string.check_user_name), Toast.LENGTH_LONG).show();
            return;
        }else if(password.equals("")){
            Toast.makeText(this, res.getString(R.string.check_pwd), Toast.LENGTH_LONG).show();
            return;
        }else{
            if(!LoadingDialog.isShowing())
            LoadingDialog.showQuitWindow(this);
            httpUtil.clearCookie();
            Map<String,String> params=new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            httpUtil.postAsynHttp(URLConfig.getUrl_login(),params,this);
        }
    }

    @Override
    public void onSuccess(final ReturnData responseBody) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.dismiss();
                if(responseBody.isStatus()){
//                    Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    User user= JSON.parseObject(responseBody.getData().toString(),User.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable("user",user);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE); //私有数据
                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                    if(checkbox.getTag().toString().equals("true")){
                        editor.putString("username",edt_username.getText().toString());
                        editor.putString("password", edt_password.getText().toString());
                    }else{
                        editor.remove("username");
                        editor.remove("password");
                    }
                    editor.commit();//提交修改
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, responseBody.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onFailure(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
