package com.sheng.android.policetalk.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.activity.LoginActivity;
import com.sheng.android.policetalk.activity.MainActivity;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.service.WebSocketService;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import freemarker.template.utility.StringUtil;

/**
 * Created by Administrator on 2017/4/8.
 */

public class RepwdDialog extends Dialog implements HttpCallBack{
    @Bind(R.id.pwd_now)
    EditText pwd_now;
    @Bind(R.id.pwd_new)
    EditText pwd_new;
    @Bind(R.id.pwd_re)
    EditText pwd_re;
    @Bind(R.id.lable_title)
    TextView lable_title;
    @Bind(R.id.btn_submit)
    Button btn_submit;

    private Resources res;
    private final int http_fail=1001;
    private final int http_success=1002;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case http_fail:
                    LoadingDialog.dismiss();
                    Toast.makeText(getContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    show();
                    break;
                case http_success:
                    LoadingDialog.dismiss();
                    ReturnData responseBody=(ReturnData)msg.obj;
                    Toast.makeText(getContext(), responseBody.getMessage(), Toast.LENGTH_LONG).show();
                    if(responseBody.isStatus()){
                        pwd_now.setText("");
                        pwd_new.setText("");
                        pwd_re.setText("");
                        dismiss();
                    }else{
                        show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public RepwdDialog(Context context, int theme) {
        super(context, theme);
    }
    public RepwdDialog(Context context){
        super(context, R.style.Theme_Transparent);
        setContentView(R.layout.dialog_repwd);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.alpha=0.5f;
        getWindow().setAttributes(lp);
        Locale myLocale;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String language=sharedPreferences.getString("language","中文");
        if(language.equals("中文")){
            myLocale = new Locale("zh");
        }else{
            myLocale = new Locale("en");
        }
        res = getContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        lable_title.setText(res.getString(R.string.modify_pwd));
        pwd_now.setHint(res.getString(R.string.pwd_current));
        pwd_new.setHint(res.getString(R.string.pwd_new));
        pwd_re.setHint(res.getString(R.string.pwd_new_re));
        btn_submit.setText(res.getString(R.string.btn_submit));
    }
    @OnClick(R.id.btn_submit)
    public void submit(View v){
        if(pwd_now.getText().toString().trim().equals("")){
            Toast.makeText(getContext(),res.getString(R.string.pwd_current),Toast.LENGTH_SHORT).show();
        }else if(pwd_new.getText().toString().trim().equals("")){
            Toast.makeText(getContext(),res.getString(R.string.pwd_new),Toast.LENGTH_SHORT).show();
        }else if(pwd_re.getText().toString().trim().equals("")){
            Toast.makeText(getContext(),res.getString(R.string.pwd_new_re),Toast.LENGTH_SHORT).show();
        }else if(!pwd_re.getText().toString().trim().equals(pwd_new.getText().toString().trim())){
            Toast.makeText(getContext(),res.getString(R.string.pwd_not_equal),Toast.LENGTH_SHORT).show();
        }else {
            hide();
            LoadingDialog.showWindow(getContext());
            Map<String,String> params=new HashMap<>();
            params.put("pwd_now",pwd_now.getText().toString().trim());
            params.put("pwd_new",pwd_new.getText().toString().trim());
            HttpUtil.getInstance().postAsynHttp(URLConfig.getRePwd(),params,this);
        }
    }

    @Override
    public void onSuccess(ReturnData responseBody) {
        Message message1=new Message();
        message1.what=http_success;
        message1.obj=responseBody;
        myHandler.sendMessage(message1);
    }

    @Override
    public void onFailure(String message) {
        Message message1=new Message();
        message1.what=http_fail;
        message1.obj=message;
        myHandler.sendMessage(message1);
    }
}
