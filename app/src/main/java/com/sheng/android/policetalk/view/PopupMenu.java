package com.sheng.android.policetalk.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sheng.android.policetalk.R;

import java.util.Locale;

/**
 * Created by Administrator on 2017/4/15.
 */

public class PopupMenu extends PopupWindow {
    private Context context;
    private int top;
    public PopupMenu(Context context, int top, OnDismissListener dismissListener, View.OnClickListener clickListener){
        this.top=top;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View conentView = inflater.inflate(R.layout.popup_menu, null);
        conentView.findViewById(R.id.layout_member).setOnClickListener(clickListener);
        conentView.findViewById(R.id.layout_add_member).setOnClickListener(clickListener);
        TextView lable_member=(TextView)conentView.findViewById(R.id.lable_member);
        TextView lable_member_add=(TextView)conentView.findViewById(R.id.lable_member_add);
        Locale myLocale;
        SharedPreferences sharedPreferences = context.getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String language=sharedPreferences.getString("language","中文");
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
        lable_member.setText(res.getString(R.string.group_member));
        lable_member_add.setText(res.getString(R.string.group_member_add));

        setContentView(conentView);
        setFocusable(true);
        setOutsideTouchable(true);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOnDismissListener(dismissListener);
        update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
    }
    public void show(View view){
//        System.out.println("开始显示");
        showAtLocation(view, Gravity.RIGHT|Gravity.TOP,0,top);
//        showAsDropDown(view,0,0);
    }
}
