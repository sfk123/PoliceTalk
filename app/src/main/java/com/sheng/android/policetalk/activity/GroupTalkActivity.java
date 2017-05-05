package com.sheng.android.policetalk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sheng.android.policetalk.Player.AudioDecoder;
import com.sheng.android.policetalk.Player.AudioPlayer;
import com.sheng.android.policetalk.Player.AudioPlayerCallBack;
import com.sheng.android.policetalk.Player.MyAudioPlayer;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.Record.AudioRecorder;
import com.sheng.android.policetalk.adapter.Adapter_Message;
import com.sheng.android.policetalk.adapter.talk_contact_adapter;
import com.sheng.android.policetalk.dao.CommonUtils;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.SocketModel;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.modal.Voice_Message;
import com.sheng.android.policetalk.service.WebSocketService;
import com.sheng.android.policetalk.util.MyUtil;
import com.sheng.android.policetalk.view.PopupGroupMember;
import com.sheng.android.policetalk.view.PopupMenu;
import com.sheng.android.policetalk.view.PopupSelectUsers;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by Administrator on 2017/3/20.
 */

public class GroupTalkActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener,RecyclerArrayAdapter.OnItemClickListener, PopupWindow.OnDismissListener{
    @Bind(R.id.tv_title)
    TextView tv_title;
    @Bind(R.id.status_img)
    ImageView status_img;
    @Bind(R.id.tv_status)
    TextView tv_status;
    @Bind(R.id.btn_group)
    FloatingActionButton btn_group;
    @Bind(R.id.content_main)
    FrameLayout content_main;
    @Bind(R.id.swiperefreshlayout)
    public SwipeRefreshLayout swiperefreshlayout;
    @Bind(R.id.recycler_message)
    RecyclerView recycler_message;
    @Bind(R.id.animate_voice)
    ImageView animate_voice;
    @Bind(R.id.mask)
    View mask;
    @Bind(R.id.btn_talk)
    Button btn_talk;
    private View actionBar_view;

    private User user;
    private Group group;
//    private talk_contact_adapter contact_adapter;
    private ProgressDialog myDialog;
    private boolean isInitOK=false;

    private List<Voice_Message> groupMessageList;
    private Resources res;
    private CommonUtils commonUtils;
    private boolean hasMorePage=true;
    private Adapter_Message adapter_message;
    private LinearLayoutManager linearLayoutManager;
    private final int connect_timeout=1001;
    private DecimalFormat df = new DecimalFormat("######0");
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case connect_timeout:
                    if(myDialog!=null&&myDialog.isShowing()){
                        myDialog.dismiss();
                    }
                    Toast.makeText(GroupTalkActivity.this,"连接超时，请重试",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case AudioRecorder.voice_update:
                    double mean=(double)msg.obj;
                    double volume =  Math.abs(mean)/214;
                    System.out.println(volume);
                    if(volume<1)
                        volume=1;
                    else if(volume>14)
                        volume=14;
                    String v=df.format(volume);
                    if(v.equals("1"))
                        animate_voice.setImageResource(R.mipmap.record_animate_01);
                    else if(v.equals("2"))
                        animate_voice.setImageResource(R.mipmap.record_animate_02);
                    else if(v.equals("3"))
                        animate_voice.setImageResource(R.mipmap.record_animate_03);
                    else if(v.equals("4"))
                        animate_voice.setImageResource(R.mipmap.record_animate_04);
                    else if(v.equals("5"))
                        animate_voice.setImageResource(R.mipmap.record_animate_05);
                    else if(v.equals("6"))
                        animate_voice.setImageResource(R.mipmap.record_animate_06);
                    else if(v.equals("7"))
                        animate_voice.setImageResource(R.mipmap.record_animate_07);
                    else if(v.equals("8"))
                        animate_voice.setImageResource(R.mipmap.record_animate_08);
                    else if(v.equals("9"))
                        animate_voice.setImageResource(R.mipmap.record_animate_09);
                    else if(v.equals("10"))
                        animate_voice.setImageResource(R.mipmap.record_animate_10);
                    else if(v.equals("11"))
                        animate_voice.setImageResource(R.mipmap.record_animate_11);
                    else if(v.equals("12"))
                        animate_voice.setImageResource(R.mipmap.record_animate_12);
                    else if(v.equals("13"))
                        animate_voice.setImageResource(R.mipmap.record_animate_13);
                    else if(v.equals("14"))
                        animate_voice.setImageResource(R.mipmap.record_animate_14);

                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_group);
        ButterKnife.bind(this);

        setLanguage();
        group=getIntent().getParcelableExtra("group");
        if(group==null){
            Toast.makeText(this,res.getString(R.string.login_expired),Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        user=getIntent().getParcelableExtra("user");
        ActionBar actionBar=getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar_view=actionBar.getCustomView();
        actionBar_view.findViewById(R.id.img_back).setOnClickListener(this);
        actionBar_view.findViewById(R.id.img_menu).setVisibility(View.VISIBLE);
        actionBar_view.findViewById(R.id.img_menu).setOnClickListener(this);
        tv_title=(TextView)actionBar_view.findViewById(R.id.tv_title);
        tv_title.setText(group.getName());
//        List<User> users=group.getMambers();
        sortUsers();
//        contact_adapter=new talk_contact_adapter(users,this);
        EventBus.getDefault().register(this);
        ViewTreeObserver observer=content_main.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//                System.out.println("height:"+content_main.getHeight());
                FrameLayout.LayoutParams flp=(FrameLayout.LayoutParams)btn_group.getLayoutParams();
                flp.topMargin=content_main.getHeight()- MyUtil.ToDipSize(80,getApplicationContext());
                btn_group.setLayoutParams(flp);
                content_main.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        commonUtils=new CommonUtils(this);
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recycler_message.setLayoutManager(linearLayoutManager);
        //添加分隔线
//        recycler_message.addItemDecoration(new MyDecoration(this, OrientationHelper.VERTICAL));
        //设置刷新时动画的颜色，可以设置4个
        swiperefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swiperefreshlayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        swiperefreshlayout.setOnRefreshListener(this);
        groupMessageList=commonUtils.getGroupMessage(user.getId(),group.getId(),1,0);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println("消息列表："+groupMessageList.size());
        recycler_message.setAdapter(adapter_message=new Adapter_Message(this,groupMessageList,user,this,group));
        if(groupMessageList.size()>1)
        recycler_message.smoothScrollToPosition(adapter_message.getItemCount() - 1);
        AudioRecorder.getInstance().setHandler(myHandler);
        SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String volume=sharedPreferences.getString("volume","1");
        double volume_double=Double.parseDouble(volume);
        int volume_int=(int)((volume_double-1)*6+1);
        MyAudioPlayer.getInstence().setVolume(volume_int);
        AudioDecoder.getInstance().setVolume(volume_int);
    }
    private void setLanguage(){
        Locale myLocale;
        SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE);
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
        btn_talk.setText(res.getString(R.string.touch_talk));
        tv_status.setText(res.getString(R.string.free));
    }
    @Override
    protected  void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        myDialog = ProgressDialog.show(this, res.getString(R.string.connecting), res.getString(R.string.connect_wait), true, true);
        JSONObject json=new JSONObject();
        json.put("type","group");
        json.put("client_id", user.getId());
        json.put("old_id",group.getId());
        group=intent.getParcelableExtra("group");
        json.put("new_id",group.getId());
//        System.out.println("onNewIntent--------------------------------------------------->"+targetUser.getUsername());
        tv_title.setText(group.getName());
        List<Voice_Message> groupMessageList=commonUtils.getGroupMessage(user.getId(),group.getId(),1,0);

        adapter_message.setMessageList(groupMessageList,group);
        EventModal eventModal=new EventModal();
        eventModal.setData(json);
        EventBus.getDefault().post(eventModal,"chang_talk");
    }
    private Boolean first=true;
    private boolean isbackground=false;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(first){
            if(!isInitOK) {
                myDialog = ProgressDialog.show(this, res.getString(R.string.connecting), res.getString(R.string.connect_wait), true, true);
                myDialog.setCancelable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!isInitOK&&!isbackground) {
                            Message message = new Message();
                            message.what = connect_timeout;
                            myHandler.sendMessage(message);
                        }
                    }
                }).start();
            }
            first = false;
        }
    }
    private User getUser(int user_id){
        for(User user:group.getMambers()){
            if(user.getId()==user_id)
                return user;
        }
        return null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume------------------------");
        if(!isInitOK) {
            isbackground=false;
            System.out.println("重新初始化群聊");
            EventModal eventModal = new EventModal();
            eventModal.setType("group");
            eventModal.setData(group.getId());
            EventBus.getDefault().post(eventModal, "initUDP");
        }
    }
    @Subscriber(tag = "UDP_init_back")
    private void connectEvent(EventModal event) {
        if(!isbackground) {
            System.out.println("UDP_init_back------------------------------------");
            if (event.getType().equals("UDP_init")) {
                isInitOK = true;
                if (myDialog != null && myDialog.isShowing()) {
                    myDialog.dismiss();
                }
//            Toast.makeText(GroupTalkActivity.this,"UDP连接成功",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Subscriber(tag = "member_talk")
    private void member_talk(EventModal event) {
        SocketModel data=(SocketModel)event.getData();
        if(data.getGroup_id()==group.getId()) {
            User user = getUser(data.getClientID());
            status_img.setImageResource(R.mipmap.media_listen_light);
            tv_status.setText(user.getUsername());
            tv_status.setTag(user.getId());
        }
    }
    @Subscriber(tag = "prepareTalk")
    private void prepareTalk(EventModal event) {
        if(event.getType().equals("back")){
            if((boolean)event.getData()){
                if(prepareRecoard) {
                    status_img.setImageResource(R.mipmap.media_talk_light);
                    tv_status.setText(res.getString(R.string.speeking));
                    animate_voice.setVisibility(View.VISIBLE);
                }else{
                    tv_status.setText(res.getString(R.string.free));
                    status_img.setImageResource(R.mipmap.media_talk_wait_light);
                }
            }
        }
    }
    @Subscriber(tag = "online")
    private void Useronline(EventModal event) {
        int userID=Integer.parseInt(event.getData().toString());
       for(User user:group.getMambers()){
           if(user.getId()==userID){
               user.setOnline(true);
               break;
           }
       }
        sortUsers();
        if(popupGroupMember!=null){
            popupGroupMember.notifyChanged();
        }
    }
    @Subscriber(tag = "offline")
    private void Useroffline(EventModal event) {
        int userID=Integer.parseInt(event.getData().toString());
        for(User user:group.getMambers()){
            if(user.getId()==userID){
                user.setOnline(false);
                if(!tv_status.getText().equals(res.getString(R.string.free))){
                    int currentID=(int)tv_status.getTag();
                    if(currentID==userID){
                        tv_status.setText(res.getString(R.string.free));
                        status_img.setImageResource(R.mipmap.media_talk_wait_light);
                    }
                }
                break;
            }
        }
        sortUsers();
        if(popupGroupMember!=null){
            popupGroupMember.notifyChanged();
        }
    }
    @Subscriber(tag = "websocket_stop_talk")
    private void websocket_stop_talk(EventModal event){//从服务器收到停止说话消息
        SocketModel data=(SocketModel)event.getData();
        if(data.getGroup_id()==group.getId()) {
            if(!tv_status.getText().equals(res.getString(R.string.free))){
                int currentID=(int)tv_status.getTag();
                if(currentID==data.getClientID()){
                    tv_status.setText(res.getString(R.string.free));
                    status_img.setImageResource(R.mipmap.media_talk_wait_light);
                }
            }
        }
    }
    @Subscriber(tag = "RecorderBack")
    private void MediaRecorder(EventModal event){//自己语音文件录制完成
        long time=Long.parseLong(event.getType());
        if(time/1000==0)
            return;
        Voice_Message group_message=new Voice_Message();
        group_message.setRead(true);
        group_message.setTarget_id(group.getId());
        group_message.setUser_id(user.getId());
        group_message.setVoice_length((int)time);
        group_message.setVoice_path(event.getData().toString());
        group_message.setDate_time(new Date());
        adapter_message.addItem(group_message);
        recycler_message.smoothScrollToPosition(adapter_message.getItemCount()-1);
    }
    @Subscriber(tag = "voiceData")
    private void voiceData(EventModal event){//从服务器收到群组语音消息
        Voice_Message group_message=(Voice_Message)event.getData();
        if(group_message.getType()==1) {
            group_message.setRead(true);
            commonUtils.MessageReaded(group_message);
            adapter_message.addItem(group_message);
            recycler_message.smoothScrollToPosition(adapter_message.getItemCount() - 1);
            tv_status.setText(res.getString(R.string.free));
            status_img.setImageResource(R.mipmap.media_talk_wait_light);
        }
    }
    @Subscriber(tag = "deleteUser")//后台删除用户
    private void deleteUser(EventModal event) {
        int user_id=(int)event.getData();
        for(User user:group.getMambers()){
            if(user.getId()==user_id){
                group.getMambers().remove(user);
                break;
            }
        }
        sortUsers();
        if(popupGroupMember!=null){
            popupGroupMember.notifyChanged();
        }
    }
    @Subscriber(tag = "addUserToGroup")//后台添加用户到群组
    private void addUserToGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        User user=JSON.parseObject(data.getData().toString(),User.class);
        if(data.getGroup_id()==group.getId()){
            group.getMambers().add(user);
            if(popupGroupMember!=null){
                popupGroupMember.addUserToGroup(user);
            }
            sortUsers();
            adapter_message.notifyDataSetChanged();
        }
//        contact_adapter.notifyDataSetChanged();
    }
    @Subscriber(tag = "removeUserOfGroup")//后台剔除群组成员
    private void removeUserOfGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        if(user.getId()==data.getClientID()){
            Toast.makeText(this,res.getString(R.string.go_out),Toast.LENGTH_SHORT).show();
            finish();
        }else {
            if (data.getGroup_id() == group.getId()) {
                for (User user : group.getMambers()) {
                    if (user.getId() == data.getClientID()) {
                        group.getMambers().remove(user);
                        if (popupGroupMember != null) {
                            popupGroupMember.removeUser(user);
                        }
                        adapter_message.notifyDataSetChanged();
                    }
                }
            }
            sortUsers();
        }
//        contact_adapter.notifyDataSetChanged();
    }
    @Subscriber(tag = "getUserIsOnline")
    private void getUserIsOnline(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        if(group.getId()==data.getGroup_id()){
            for(User user:group.getMambers()){
                if(user.getId()==data.getClientID()){
                    user.setOnline(JSON.parseObject(data.getData().toString(),Boolean.class));
                    break;
                }
            }
            sortUsers();
            if(popupGroupMember!=null){
                popupGroupMember.notifyChanged();
            }
        }
    }
    @Override
    protected void onDestroy(){
        EventBus.getDefault().unregister(this);
        EventModal eventModal=new EventModal();
        eventModal.setType("group");
        EventBus.getDefault().post(eventModal, "close_talk");
        super.onDestroy();
    }
    @Subscriber(tag = "close_talk")
    private void close_talk(EventModal event) {//网络断开时，关闭页面
        if(event.getType().equals("all")){
            Toast.makeText(getApplicationContext(),res.getString(R.string.check_network),Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Subscriber(tag = "goBackground")
    private void goBackground(EventModal event) {//当前页面被单聊窗口覆盖时
        if(event.getType().equals("group")){
//            isInitOK=false;
//            isbackground=true;
            finish();
        }

    }
    @OnTouch(R.id.btn_talk)
    public boolean onTouch(View view, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN&&!prepareRecoard) {
            if(tv_status.getText().toString().equals(res.getString(R.string.free))) {
                prepareRecoard = true;
                EventModal eventModal=new EventModal();
                eventModal.setType("prepare");
                EventBus.getDefault().post(eventModal, "prepareTalk");
            }else{
                prepareRecoard = true;
                Toast.makeText(this,res.getString(R.string.line_busy),Toast.LENGTH_SHORT).show();
            }
        }else if(event.getAction() ==MotionEvent.ACTION_UP){
            prepareRecoard=false;
            stopTalk();
        }
        return false;
    }
    @Subscriber(tag = "logout")//当前账号在别的地方登录
    private void logout(EventModal event) {
        finish();
    }
    private void sortUsers(){
        Collections.sort(group.getMambers(), new Comparator<User>() {
            @Override
            public int compare(User u1,User u2){
                if(u1.getOnline()&&u2.getOnline()){
                    return 0;
                }else if(u1.getOnline()){
                    return -1;
                }else if(u2.getOnline()){
                    return 1;
                }else{
                    return 0;
                }
            }
        });
    }
    @Subscriber(tag = "userEvent")
    private void userEvent(EventModal event) {
        if(event.getType().equals("changePhoto")){//用户更改头像
            SocketModel data=(SocketModel)event.getData();
            User user_temp= JSON.parseObject(data.getData().toString(),User.class);
            for(User user:group.getMambers()){
                if(user.getId()==user_temp.getId()){
                    user.setPhoto(user_temp.getPhoto());
                    adapter_message.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
    @Subscriber(tag = "group_event")
    private void group_event(EventModal event) {
        if(event.getType().equals("addGroupManager")){
            SocketModel socketModel=(SocketModel)event.getData();
            User user1=JSON.parseObject(socketModel.getData().toString(),User.class);
            if(group.getId()==socketModel.getGroup_id()){
                    group.getManagers().add(user1);
            }
        }
    }
    private boolean prepareRecoard=false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        final int keyCode = event.getKeyCode();
        final int action=event.getAction();
        if(MotionEvent.ACTION_DOWN==action&&keyCode==58&&!prepareRecoard){//按下
            if(tv_status.getText().toString().equals(res.getString(R.string.free))) {
                prepareRecoard = true;
                EventModal eventModal=new EventModal();
                eventModal.setType("prepare");
                EventBus.getDefault().post(eventModal, "prepareTalk");
            }else{
                prepareRecoard = true;
                Toast.makeText(this,res.getString(R.string.line_busy),Toast.LENGTH_SHORT).show();
            }
        }else if(MotionEvent.ACTION_UP==action&&keyCode==58){//松开
            prepareRecoard=false;
                stopTalk();
        }
        return super.dispatchKeyEvent(event);
    }
    private void stopTalk(){
        if(tv_status.getText().toString().equals(res.getString(R.string.speeking))) {
            tv_status.setText(res.getString(R.string.free));
            status_img.setImageResource(R.mipmap.media_talk_wait_light);
        }
        EventBus.getDefault().post(new EventModal(), "stopTalk");
        animate_voice.setVisibility(View.GONE);
    }
//    @OnClick(R.id.btn_group)
//    public void groupShow(View v){
//        if(!isMove) {
//            if (layout_contact.getVisibility()==View.VISIBLE) {
//                layout_contact.setVisibility(View.GONE);
//            } else {
//                layout_contact.setVisibility(View.VISIBLE);
//            }
//        }else{
//            isMove=false;
//        }
//    }
    private int btn_group_top=0,btn_group_left=0;
    private float x=0,y=0;
    private boolean isMove=false;
    @OnTouch(R.id.btn_group)
    public boolean onTouch_btn_group(View view, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            FrameLayout.LayoutParams flp=(FrameLayout.LayoutParams)btn_group.getLayoutParams();
            btn_group_top=flp.topMargin;
            btn_group_left=flp.leftMargin;
            x=event.getX();
            y=event.getY();
        }else if(event.getAction() ==MotionEvent.ACTION_MOVE){
            float x1=event.getX()-x;
            float y1=event.getY()-y;
            if(x1>5||y1>5){
                isMove=true;
            }
            FrameLayout.LayoutParams flp=(FrameLayout.LayoutParams)btn_group.getLayoutParams();
            btn_group_top=flp.topMargin+(int)y1;
            btn_group_left=flp.leftMargin+(int)x1;
            flp.topMargin=btn_group_top;
            flp.leftMargin=btn_group_left;
            btn_group.setLayoutParams(flp);
        }
        return false;
    }
    private PopupMenu popupMenu;
    private PopupGroupMember popupGroupMember;
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.img_back){
            finish();
        }else if(v.getId()==R.id.img_menu){
            mask.setVisibility(View.VISIBLE);
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            int actionBarHeight = getSupportActionBar().getHeight();
            popupMenu=new PopupMenu(this,statusBarHeight+actionBarHeight,this,this);
            popupMenu.show(actionBar_view);
        }else if(v.getId()==R.id.layout_member){
            popupMenu.dismiss();
            mask.setVisibility(View.VISIBLE);
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            int actionBarHeight = getSupportActionBar().getHeight();
            popupGroupMember=new PopupGroupMember(this,statusBarHeight+actionBarHeight,this,group,this);
            popupGroupMember.setCurrentUser(user);
            popupGroupMember.show(actionBar_view);
        }else if(v.getId()==R.id.layout_add_member){
            boolean cando=false;
            for(User user1:group.getManagers()){
                if(user1.getId()==user.getId()){
                    cando=true;
                    break;
                }
            }
            if(cando) {
                popupMenu.dismiss();
                mask.setVisibility(View.VISIBLE);
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                int actionBarHeight = getSupportActionBar().getHeight();
                PopupSelectUsers popupSelectUsers = new PopupSelectUsers(this,this, statusBarHeight + actionBarHeight, group.getId());
                popupSelectUsers.show(actionBar_view);
            }else{
                Toast.makeText(this,res.getString(R.string.no_promis_add_user),Toast.LENGTH_SHORT).show();
            }
        }else {
            final ImageView img_sound = (ImageView) v.findViewById(R.id.img_sound);
            final Drawable old_drawable = img_sound.getDrawable();
            Object item = v.getTag();
            if (item instanceof Voice_Message) {//群消息
                final Voice_Message group_message = (Voice_Message) item;
                if (group_message.getUser_id() == user.getId()) {//自己发的消息
                    img_sound.setImageResource(R.drawable.voice_play_right);
                    AnimationDrawable drawable = (AnimationDrawable) img_sound.getDrawable();
                    drawable.start();
                    MyAudioPlayer.getInstence().start(group_message.getVoice_path(), new AudioPlayerCallBack() {
                        @Override
                        public void onComplete() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    img_sound.setImageDrawable(old_drawable);
                                }
                            });
                        }
                    });
                } else {//别人发的消息
                    img_sound.setImageResource(R.drawable.voice_play_left);
                    AnimationDrawable drawable = (AnimationDrawable) img_sound.getDrawable();
                    drawable.start();
                    MyAudioPlayer.getInstence().start(group_message.getVoice_path(), new AudioPlayerCallBack() {
                        @Override
                        public void onComplete() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    img_sound.setImageDrawable(old_drawable);
                                    if(!group_message.getRead()){
                                        group_message.setRead(true);
                                        commonUtils.MessageReaded(group_message);
                                        adapter_message.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }
    }
//    @Override
//    public void onBackPressed() {
//        if (layout_contact.getVisibility()==View.VISIBLE) {
//            layout_contact.setVisibility(View.GONE);
//        } else {
//            super.onBackPressed();
//        }
//    }
    @Override
    public void onRefresh() {//加载更多数据
        if(hasMorePage) {
            List<Voice_Message> groupMessageList;
            if(this.groupMessageList!=null&&this.groupMessageList.size()>0) {
                Voice_Message first=adapter_message.getFirst();
                long first_id=0;
                if(first!=null)
                    first_id=first.getId();
                groupMessageList= commonUtils.getGroupMessage(user.getId(), group.getId(),  1, first_id);
                System.out.println("得到数据："+groupMessageList.size());
                for(Voice_Message vm:groupMessageList){
                    System.out.println(vm.getDate_time().getTime());
                }
//                System.out.println(groupMessageList.get(0).getDate_time().getTime());
                System.out.println("刷新后第一条时间："+this.groupMessageList.get(0).getDate_time().getTime());
                adapter_message.addItems(groupMessageList);
                if(groupMessageList.size()<10){
                    hasMorePage=false;
                }
            }else{
                Toast.makeText(this,res.getString(R.string.no_more),Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,res.getString(R.string.no_more),Toast.LENGTH_SHORT).show();
        }
        swiperefreshlayout.setRefreshing(false);
    }
    @Override
    public void onDismiss() {
        mask.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int position) {
        User target_user=group.getMambers().get(position);
        if(target_user.getId()==this.user.getId()){
            Toast.makeText(this,res.getString(R.string.not_me),Toast.LENGTH_SHORT).show();
        }else if(!target_user.getOnline()){
            Toast.makeText(this,res.getString(R.string.target_not_online),Toast.LENGTH_SHORT).show();
        }else{
            isInitOK=false;
            isbackground=true;
            Intent intent = new Intent(this, OneToOneTalkActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("currentUser", this.user);
            mBundle.putParcelable("targetUser", target_user);
            intent.putExtras(mBundle);
            startActivity(intent);
        }
    }
}
