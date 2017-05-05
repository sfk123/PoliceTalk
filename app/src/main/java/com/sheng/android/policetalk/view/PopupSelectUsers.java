package com.sheng.android.policetalk.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.adapter.Adapter_SelectUser;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.util.MyUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/15.
 */

public class PopupSelectUsers extends PopupWindow implements HttpCallBack,View.OnClickListener{
    private Context context;
    private int top;
    private int group_id;
    private final int http_success=1001;
    private final int http_fail=1002;
    private int http_type=1;
    private Adapter_SelectUser adapterSelectUser;
    private RelativeLayout layout_content_body;
    private Button btn_add;
    private TextView tv_loading;
    private RecyclerView recycler;
    private Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case http_success:
                    ReturnData responseBody=(ReturnData)msg.obj;
                    if(LoadingDialog.isShowing())
                        LoadingDialog.dismiss();
                    if(responseBody.isStatus()){
                        if(http_type==1) {
                            List<User> users = JSON.parseArray(responseBody.getData().toString(), User.class);
                            layout_content_body.setVisibility(View.VISIBLE);
                            tv_loading.setVisibility(View.GONE);
                            adapterSelectUser = new Adapter_SelectUser(context, users);
                            recycler.setAdapter(adapterSelectUser);
                        }else if(http_type==2){
                            Toast.makeText(context,responseBody.getMessage(),Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }else
                    Toast.makeText(context,responseBody.getMessage(),Toast.LENGTH_SHORT).show();
                    break;
                case http_fail:
                    if(LoadingDialog.isShowing())
                        LoadingDialog.dismiss();
                    Toast.makeText(context,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public PopupSelectUsers(Activity activity,OnDismissListener dismissListener,int top,int group_id){
        this.context=activity;
        this.top=top;
        this.group_id=group_id;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View conentView = inflater.inflate(R.layout.popup_select_users, null);
        setContentView(conentView);
        layout_content_body=(RelativeLayout)conentView.findViewById(R.id.layout_content_body);
        btn_add=(Button)conentView.findViewById(R.id.btn_add);
        tv_loading=(TextView)conentView.findViewById(R.id.tv_loading);
        recycler=(RecyclerView)conentView.findViewById(R.id.recycler);
        btn_add.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recycler.setLayoutManager(linearLayoutManager);
        //添加分隔线
        recycler.addItemDecoration(new MyDecoration(activity, OrientationHelper.VERTICAL));
//        recycler.seton
        setFocusable(true);
        setOutsideTouchable(true);
        int w = activity.getWindowManager().getDefaultDisplay().getWidth();
        int h = activity.getWindowManager().getDefaultDisplay().getHeight();
        this.setWidth(w*3/4);
        this.setHeight(h-top- MyUtil.ToDipSize(50,activity));
        setOnDismissListener(dismissListener);
        update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        SharedPreferences sharedPreferences = context.getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String language=sharedPreferences.getString("language","中文");
        Locale myLocale;
        if(language.equals("中文")){
            myLocale = new Locale("zh");
        }else{
            myLocale = new Locale("en");
        }
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        btn_add.setText(res.getString(R.string.btn_add));
        tv_loading.setText(res.getString(R.string.loading));
    }
    public void show(View view){
        http_type=1;
        showAtLocation(view, Gravity.TOP,0,top);
        Map<String,String> parameters=new HashMap<>();
        parameters.put("group_id",String.valueOf(group_id));
        HttpUtil.getInstance().postAsynHttp(URLConfig.getUserOfAddToGroup(),parameters,this);
    }

    @Override
    public void onSuccess(ReturnData responseBody) {
        Message msg=new Message();
        msg.what=http_success;
        msg.obj=responseBody;
        handler.sendMessage(msg);
    }

    @Override
    public void onFailure(String message) {
        Message msg=new Message();
        msg.what=http_fail;
        msg.obj=message;
        handler.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_add){
            List<String> select_ids=adapterSelectUser.getSelectIds();
            if(select_ids.size()>0){
                LoadingDialog.showWindow(context);
                http_type=2;
                Map<String,String> parameters=new HashMap<>();
                parameters.put("group_id",String.valueOf(group_id));
                parameters.put("member_ids",JSON.toJSONString(select_ids));
                HttpUtil.getInstance().postAsynHttp(URLConfig.addUserToGroup(),parameters,this);
            }else{
                Toast.makeText(context,"请选择要添加的人员",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
