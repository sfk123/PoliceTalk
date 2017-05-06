package com.sheng.android.policetalk.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.adapter.GroupMemberAdapter;
import com.sheng.android.policetalk.adapter.SwipeItemOnItemChildClickListener;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;

/**
 * Created by Administrator on 2017/4/15.
 */

public class PopupGroupMember extends PopupWindow implements SwipeItemOnItemChildClickListener,HttpCallBack{
    private Context context;
    private int top;
    private GroupMemberAdapter adapter;
    private RecyclerArrayAdapter.OnItemClickListener onItemClickListener;
    private Group group;
    private User currentUser;
    private final int http_success=1001;
    private final int http_fail=1002;
    private int currentPosition=0;
    private Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
        // TODO Auto-generated method stub
            switch (msg.what){
                case http_success:
                    if(LoadingDialog.isShowing())
                        LoadingDialog.dismiss();
                    ReturnData responseBody=(ReturnData)msg.obj;
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
    public PopupGroupMember(Activity context, int top, OnDismissListener dismissListener, Group group, RecyclerArrayAdapter.OnItemClickListener onItemClickListener,User currentUser){
        this.top=top;
        this.group=group;
        this.currentUser=currentUser;
        this.context=context;
        this.onItemClickListener=onItemClickListener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View conentView = inflater.inflate(R.layout.popup_group_member, null);
        setContentView(conentView);
        initView((EasyRecyclerView)conentView.findViewById(R.id.recyclerview));
        setFocusable(true);
        setOutsideTouchable(true);
        // 设置SelectPicPopupWindow弹出窗体的宽
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        this.setWidth(w/2);
        // 设置SelectPicPopupWindow弹出窗体的高
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        this.setHeight(h-top);
        setOnDismissListener(dismissListener);
        update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
    }

    public void addUserToGroup(User user) {
        adapter.add(user);
    }
    public void removeUser(User user){
        adapter.remove(user);
    }
    public void notifyChanged(){
        adapter.notifyDataSetChanged();
    }
    private void initView(EasyRecyclerView recyclerView){
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

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new ScaleInBottomAnimator(new OvershootInterpolator(1f)));
        recyclerView.setAdapter(adapter = new GroupMemberAdapter(context,res));
        adapter.setSwipeItemOnItemChildClickListener(this);//设置侧滑菜单控件点击监听
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    adapter.closeOpenedSwipeItemLayoutWithAnim();
                }
            }
        });
        List<User> users=new ArrayList<>();
        for(User u:group.getMambers()){
            if(u.getId()!=currentUser.getId())
                users.add(u);
        }
        adapter.addAll(users);
    }

    @Override
    public void onItemChildClick(ViewGroup var1, View childView, int position) {
        if (childView.getId() == R.id.tv_item_swipe_delete) {
            boolean manager=false;
            currentPosition=position;
            for(User user:group.getManagers()){
                if(user.getId()==currentUser.getId()){
                    manager=true;
                    break;
                }
            }
            if(manager) {
                final User user_delete=group.getMambers().get(position);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // 设置显示信息
                builder.setMessage("确定要踢出成员："+user_delete.getUsername()+"?")
                        .
                        // 设置确定按钮
                                setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    // 单击事件
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        LoadingDialog.showWindow(context);
                                        Map<String,String> parameters=new HashMap<>();
                                        parameters.put("group_id",String.valueOf(group.getId()));
                                        parameters.put("user_id",String.valueOf(user_delete.getId()));
                                        HttpUtil.getInstance().postAsynHttp(URLConfig.getDeleteGroupUser(),parameters,PopupGroupMember.this);
                                    }
                                }).
                        // 设置取消按钮
                                setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                });
                // 创建对话框
                AlertDialog ad = builder.create();
                // 显示对话框
                ad.show();
            }else{
                Toast.makeText(context,"没有权限操作",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onItemChildLongClick(ViewGroup var1, View var2, int var3) {
        return false;
    }
    public void show(View view){
//        System.out.println("开始显示");
        showAtLocation(view, Gravity.LEFT|Gravity.TOP,0,top);
//        showAsDropDown(view,0,0);
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
}
