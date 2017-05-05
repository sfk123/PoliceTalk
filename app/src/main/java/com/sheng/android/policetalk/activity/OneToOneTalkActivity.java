package com.sheng.android.policetalk.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.Player.AudioDecoder;
import com.sheng.android.policetalk.Player.AudioPlayer;
import com.sheng.android.policetalk.Player.AudioPlayerCallBack;
import com.sheng.android.policetalk.Player.MyAudioPlayer;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.Record.AudioRecorder;
import com.sheng.android.policetalk.adapter.Adapter_Message;
import com.sheng.android.policetalk.dao.CommonUtils;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.SocketModel;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.modal.Voice_Message;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * Created by Administrator on 2017/3/24.
 */

public class OneToOneTalkActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{
    @Bind(R.id.swiperefreshlayout)
    public SwipeRefreshLayout swiperefreshlayout;
    @Bind(R.id.recycler_message)
    RecyclerView recycler_message;
    @Bind(R.id.status_img)
    ImageView status_img;
    @Bind(R.id.tv_status)
    TextView tv_status;
    @Bind(R.id.animate_voice)
    ImageView animate_voice;
    private TextView title;
    private User currentUser;
    private User targetUser;
    private CommonUtils commonUtils;
    private LinearLayoutManager linearLayoutManager;
    private Adapter_Message adapter_message;
    private ProgressDialog myDialog;
    private boolean hasMorePage=true;
    private boolean isInitOK=false;
    private Boolean first=true;
    private boolean isbackground=false;
    private  Group group;
    private final int connect_timeout=1001;
    private DecimalFormat df = new DecimalFormat("######0");
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case connect_timeout:
                    if(myDialog!=null&&myDialog.isShowing()){
                        myDialog.dismiss();
                    }
                    Toast.makeText(OneToOneTalkActivity.this,"连接超时，请重试",Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_talk_one2one);
        currentUser=getIntent().getParcelableExtra("currentUser");
        targetUser=getIntent().getParcelableExtra("targetUser");
        ActionBar actionBar=getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.getCustomView().findViewById(R.id.img_back).setOnClickListener(this);
        title=(TextView)actionBar.getCustomView().findViewById(R.id.tv_title);
        title.setText(targetUser.getUsername());
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        commonUtils=new CommonUtils(this);
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recycler_message.setLayoutManager(linearLayoutManager);
        swiperefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swiperefreshlayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        swiperefreshlayout.setOnRefreshListener(this);
        List<Voice_Message> groupMessageList=commonUtils.getGroupMessage(currentUser.getId(),targetUser.getId(),2,0);
        List<User> users=new ArrayList<>();
        users.add(targetUser);
        group=new Group();
        group.setMambers(users);
        recycler_message.setAdapter(adapter_message=new Adapter_Message(this,groupMessageList,currentUser,this,group));
        if(groupMessageList.size()>1)
        recycler_message.smoothScrollToPosition(adapter_message.getItemCount() - 1);
        AudioRecorder.getInstance().setHandler(myHandler);
        SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String volume=sharedPreferences.getString("volume","1");
        double volume_double=Double.parseDouble(volume);
        int volume_int=(int)((volume_double-1)*6+1);
//        System.out.println("---------------------------->volume_int:"+volume_int);
        MyAudioPlayer.getInstence().setVolume(volume_int);
        AudioDecoder.getInstance().setVolume(volume_int);
    }
    @Override
    protected  void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        JSONObject json=new JSONObject();
        json.put("type","single");
        json.put("client_id", currentUser.getId());
        json.put("old_id",targetUser.getId());
        targetUser=intent.getParcelableExtra("targetUser");
        json.put("new_id",targetUser.getId());
//        System.out.println("onNewIntent--------------------------------------------------->"+targetUser.getUsername());
        title.setText(targetUser.getUsername());
        List<Voice_Message> groupMessageList=commonUtils.getGroupMessage(currentUser.getId(),targetUser.getId(),2,0);
        List users=new ArrayList<>();
        users.add(targetUser);
        group.setMambers(users);

        adapter_message.setMessageList(groupMessageList,group);
        EventModal eventModal=new EventModal();
        eventModal.setData(json);
        EventBus.getDefault().post(eventModal,"chang_talk");
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(first){
            if(!isInitOK) {
                myDialog = ProgressDialog.show(this, "正在连接服务器..", "连接中,请稍后..", true, true);
                myDialog.setCancelable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!isInitOK) {
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
    @Override
    protected void onResume() {
        super.onResume();
        if(!isInitOK&&!isbackground) {
            System.out.println("初始化单聊");
            isbackground=true;
            EventModal eventModal=new EventModal();
            eventModal.setType("Single");
            eventModal.setData(targetUser.getId());
            EventBus.getDefault().post(eventModal, "initUDP");
        }
    }
    @Subscriber(tag = "goBackground")
    private void goBackground(EventModal event) {//当前页面被群聊窗口覆盖时
        if(event.getType().equals("single")){
//            isInitOK=false;
//            isbackground=false;
            finish();
        }
    }
    @Subscriber(tag = "UDP_init_back")
    private void connectEvent(EventModal event) {
        if(isbackground) {
            System.out.println("UDP_init_back------------------------------------");
            if (event.getType().equals("UDP_init")) {
                isInitOK = true;
                if (myDialog != null && myDialog.isShowing()) {
                    myDialog.dismiss();
                }
            }
        }
    }
    @Subscriber(tag = "voiceData")
    private void voiceData(EventModal event){//从服务器收到群组语音消息
        Voice_Message group_message=(Voice_Message)event.getData();
        if(group_message.getType()==2) {
            if(!group_message.getRead()) {
                group_message.setRead(true);
                commonUtils.MessageReaded(group_message);
            }
            adapter_message.addItem(group_message);
            recycler_message.smoothScrollToPosition(adapter_message.getItemCount() - 1);
        }
    }
    @Subscriber(tag = "userEvent")
    private void userEvent(EventModal event) {
        if(event.getType().equals("changePhoto")){//用户更改头像
            SocketModel data=(SocketModel)event.getData();
            User user_temp= JSON.parseObject(data.getData().toString(),User.class);
            if(targetUser.getId()==user_temp.getId()){
                targetUser.setPhoto(user_temp.getPhoto());
                group.getMambers().get(0).setPhoto(user_temp.getPhoto());
                adapter_message.notifyDataSetChanged();
            }
        }
    }
    @Subscriber(tag = "close_talk")
    private void close_talk(EventModal event) {//网络断开时，关闭页面
        if(event.getType().equals("all")){
            Toast.makeText(getApplicationContext(),"请检查网络后重试",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Subscriber(tag = "logout")//当前账号在别的地方登录
    private void logout(EventModal event) {
        finish();
    }
    @Subscriber(tag = "UDP_single")//单聊UDP消息相关
    private void UDP_init_single(EventModal event) {
        if(event.getType().equals("start_talk_single")){
            SocketModel data=(SocketModel)event.getData();
            if(targetUser.getId()==data.getClientID()){
                status_img.setImageResource(R.mipmap.media_listen_light);
                tv_status.setText("对方正在说话");
            }
        }else if(event.getType().equals("stop_talk_single")){
            SocketModel data=(SocketModel)event.getData();
            if(targetUser.getId()==data.getClientID()){
                status_img.setImageResource(R.mipmap.media_talk_wait_light);
                tv_status.setText("空闲");
            }
        }
    }
    @Subscriber(tag = "prepareTalk")
    synchronized private void prepareTalk(EventModal event) {
        if(event.getType().equals("back")){
            boolean status=(boolean)event.getData();
            if(status){
                if(prepareRecoard) {
                    status_img.setImageResource(R.mipmap.media_talk_light);
                    tv_status.setText("正在说话");
                    animate_voice.setVisibility(View.VISIBLE);

                }else{
                    System.out.println("已经停止说话");
                }
            }
        }
    }
    @Subscriber(tag = "offline")
    private void Useroffline(EventModal event) {
        int userID=Integer.parseInt(event.getData().toString());
        if(userID==targetUser.getId()){
            if(tv_status.getText().toString().equals("对方正在说话")){
                status_img.setImageResource(R.mipmap.media_talk_wait_light);
                tv_status.setText("空闲");
            }
            Toast.makeText(this,"对方已离线",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.img_back){
            finish();
        }else{
            final ImageView img_sound=(ImageView)v.findViewById(R.id.img_sound);
            final Drawable old_drawable=img_sound.getDrawable();
            Object item=v.getTag();
            if(item instanceof Voice_Message){//群消息
                final Voice_Message group_message=(Voice_Message)item;
                if(group_message.getUser_id()==currentUser.getId()){//自己发的消息
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
                }else{//别人发的消息
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
    private boolean prepareRecoard=false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        final int keyCode = event.getKeyCode();
        final int action=event.getAction();
        if(MotionEvent.ACTION_DOWN==action&&keyCode==58&&!prepareRecoard){//按下
            if(tv_status.getText().toString().equals("空闲")) {
                prepareRecoard = true;
                EventModal eventModal = new EventModal();
                eventModal.setType("prepare");
                EventBus.getDefault().post(eventModal, "prepareTalk");
            }else{
                Toast.makeText(this,"线路忙，请稍后",Toast.LENGTH_SHORT).show();
            }
        }else if(MotionEvent.ACTION_UP==action&&keyCode==58){//松开
            prepareRecoard=false;
            EventBus.getDefault().post(new EventModal(), "stopTalk");
            if(tv_status.getText().toString().equals("正在说话")){
                status_img.setImageResource(R.mipmap.media_talk_wait_light);
                tv_status.setText("空闲");
            }
            animate_voice.setVisibility(View.GONE);
        }
        return super.dispatchKeyEvent(event);
    }
    @OnTouch(R.id.btn_talk)
    public boolean onTouch(View view, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN&&!prepareRecoard) {
            if(tv_status.getText().toString().equals("空闲")) {
                prepareRecoard = true;
                EventModal eventModal=new EventModal();
                eventModal.setType("prepare");
                EventBus.getDefault().post(eventModal, "prepareTalk");
            }else{
                prepareRecoard = true;
                Toast.makeText(this,"线路忙请稍后",Toast.LENGTH_SHORT).show();
            }
        }else if(event.getAction() ==MotionEvent.ACTION_UP){
            prepareRecoard=false;
            stopTalk();
        }
        return false;
    }
    private void stopTalk(){
        if(tv_status.getText().toString().equals("正在说话")) {
            tv_status.setText("空闲");
            status_img.setImageResource(R.mipmap.media_talk_wait_light);
        }
        EventBus.getDefault().post(new EventModal(), "stopTalk");
        animate_voice.setVisibility(View.GONE);
    }
    @Override
    protected void onDestroy(){
        EventBus.getDefault().unregister(this);
        EventModal eventModal=new EventModal();
        eventModal.setType("single");
        EventBus.getDefault().post(eventModal, "close_talk");
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if(hasMorePage) {
            Voice_Message first=adapter_message.getFirst();
            long first_id=0;
            if(first!=null)
                first_id=first.getId();
            List<Voice_Message> groupMessageList = commonUtils.getGroupMessage(currentUser.getId(),targetUser.getId(),2,first_id);
            adapter_message.addItems(groupMessageList);
            if(groupMessageList.size()<10){
                hasMorePage=false;
            }
        }else{
            Toast.makeText(this,"没有更多记录了",Toast.LENGTH_SHORT).show();
        }
        swiperefreshlayout.setRefreshing(false);
    }
}
