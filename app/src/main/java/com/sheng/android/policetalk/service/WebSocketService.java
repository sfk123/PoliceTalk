package com.sheng.android.policetalk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.AudioWrapper;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.Record.AudioEncoder;
import com.sheng.android.policetalk.UDP.UDPService;
import com.sheng.android.policetalk.UDP.UDPServiceStopCallBack;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.WebSocket.WebSocketConnection;
import com.sheng.android.policetalk.activity.GroupTalkActivity;
import com.sheng.android.policetalk.activity.LoginActivity;
import com.sheng.android.policetalk.activity.OneToOneTalkActivity;
import com.sheng.android.policetalk.dao.CommonUtils;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.SocketModel;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.modal.VoiceData;
import com.sheng.android.policetalk.modal.Voice_Message;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.util.MyUtil;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/3/20.
 */

public class WebSocketService extends Service implements HttpCallBack {
    private WebSocketConnection webSocketConnection;
    private User user;
    private List<Group> groups;
    private HttpUtil httpUtil;
    private MediaPlayer mediaPlayer;

    private BroadcastReceiver broadcastReceiver;
    public static final int KEYCODE_PTT = 264;
    private boolean talk_wait=true;
    private CommonUtils commonUtils;
    private String type;//group(当前聊天是群组) single(当前是单聊)
    private int target_id;//若当前是群聊 则为group_id,若是单聊 则是聊天对象userid
    private Boolean prepareRecoard=false;
    private Lock lock;
    private final int http_false=1;
    private String path;//语音文件存储路径
    private Resources res;
    private boolean isChina=true;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case http_false:
                    Toast.makeText(getApplicationContext(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        webSocketConnection=WebSocketConnection.getInstence();
        httpUtil=HttpUtil.getInstance();
        EventBus.getDefault().register(this);
        mediaPlayer= MediaPlayer.create(this, R.raw.sound_media_me_on_low);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<开始说话");
                if(prepareRecoard) {
                    AudioWrapper.getInstance().startRecord();
                    SocketModel data = new SocketModel();
                    data.setClientID(user.getId());
                    data.setGroup_id(target_id);
                    data.setType("start_talk_single");
                    WebSocketConnection.getInstence().sendMessage(data);
                }else{
                    System.out.println("已经停止");
                }
            }
        });
        setLanguage(null);
        File dir = Environment.getExternalStorageDirectory();
        if(dir==null){
            Toast.makeText(this,res.getString(R.string.no_sdcard),Toast.LENGTH_SHORT).show();
        }else {
//            MediaRecorderManager.getInstance().Prepare(dir.getAbsolutePath()+"/PTT");
            path=dir.getAbsolutePath()+"/PTT";
            File file=new File(path);
            if(!file.isDirectory()){
                System.out.println("创建文件夹");
                file.mkdir();
            }
        }
        commonUtils=new CommonUtils(this);
        lock=new ReentrantLock();
        broadcastReceiver=new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                KeyEvent event =intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if(KEYCODE_PTT==event.getKeyCode()){
                    EventModal eventModal=new EventModal();
                    eventModal.setType("prepare");
                    prepareTalk(eventModal);
                }
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter("android.intent.action.KEY_EVENT"));
        EventModal eventModal=new EventModal();
        eventModal.setType("getGroups_back");
        EventBus.getDefault().post(eventModal,"getGroups");

    }
    @Subscriber(tag = "changeLanguage")
    private void setLanguage(EventModal event){
            SharedPreferences sharedPreferences = getSharedPreferences("wujay", Context.MODE_PRIVATE);
            String language=sharedPreferences.getString("language","中文");
            Locale myLocale;
            if(language.equals("中文")){
                myLocale = new Locale("zh");
            }else{
                isChina=false;
                myLocale = new Locale("en");
            }
            res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
    }
    @Subscriber(tag = "isLogin")
    private void connectEvent(EventModal event) {//登录界面查看是否已经登录
        if(event.getType().equals("isLogin")){
            EventModal eventModal=new EventModal();
            eventModal.setType("isLogin");
            eventModal.setData(user);
            EventBus.getDefault().post(eventModal, "isLogin_back");
        }
    }
    @Subscriber(tag = "initUDP")
    private void initUDP(final EventModal event) {//初始化UDP连接
        if(type!=null) {
            if (type.equals("group")) {
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setGroup_id(target_id);
                data.setType("goaway");
                WebSocketConnection.getInstence().sendMessage(data);
                EventModal eventModal=new EventModal();
                eventModal.setType("group");
                EventBus.getDefault().post(eventModal,"goBackground");
            } else if (type.equals("single")) {
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setGroup_id(target_id);
                data.setType("goaway_single");
                WebSocketConnection.getInstence().sendMessage(data);
                EventModal eventModal=new EventModal();
                eventModal.setType("single");
                EventBus.getDefault().post(eventModal,"goBackground");
            }
            AudioWrapper.getInstance().stopListen(new UDPServiceStopCallBack() {
                @Override
                public void stoped() {
                    if(event.getType().equals("group")){
                        type="group";
                        target_id =Integer.parseInt(event.getData().toString());
                        SocketModel data = new SocketModel();
                        data.setClientID(user.getId());
                        data.setGroup_id(target_id);
                        data.setType("prepareUDP_Server");
                        WebSocketConnection.getInstence().sendMessage(data);
                    }else if(event.getType().equals("Single")){
                        type="single";
                        target_id =Integer.parseInt(event.getData().toString());
                        AudioWrapper.getInstance().initUDP("init_single",user.getId(), target_id,0);
                    }
                }
            });
        }else{
            if(event.getType().equals("group")){
                type="group";
                target_id =Integer.parseInt(event.getData().toString());
//            AudioWrapper.getInstance().initUDP("init_group",user.getId(), target_id);
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setGroup_id(target_id);
                data.setType("prepareUDP_Server");
                WebSocketConnection.getInstence().sendMessage(data);
            }else if(event.getType().equals("Single")){
                type="single";
                target_id =Integer.parseInt(event.getData().toString());
                AudioWrapper.getInstance().initUDP("init_single",user.getId(), target_id,0);
            }
        }

    }
    @Subscriber(tag = "UDP_group")
    private void UDP_group(EventModal event) {
        if(event.getType().equals("prepareUDP_back")){
            int port=Integer.parseInt(event.getData().toString());
            AudioWrapper.getInstance().initUDP("init_group",user.getId(), target_id,port);
        }
    }
    @Subscriber(tag = "offline")
    private void offline(EventModal event) {
//        SocketModel data=(SocketModel)event.getData();
//        if(type.equals("single")&& target_id ==data.getClientID()){
//            AudioEncoder.getInstance().stopSender();
//            AudioWrapper.getInstance().stopListen();
//            UDPBeatService.getInstence().stop();
//            UDPService.getInstence().stopPrepare();
//            port=0;
//            System.out.println("关闭发送线程");
//        }
    }
    @Subscriber(tag = "close_talk")
    private void close_talk(EventModal event) {
        System.out.println("WebSocketService:close_talk----->eventType:"+event.getType());
        if(type==null){
            System.out.println("当前没有说话界面");
            return;
        }
        if(event.getType().equals(type)) {
            if (type.equals("group")) {
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setGroup_id(target_id);
                data.setType("goaway");
                WebSocketConnection.getInstence().sendMessage(data);
            } else if (type.equals("single")) {
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setGroup_id(target_id);
                data.setType("goaway_single");
                WebSocketConnection.getInstence().sendMessage(data);
            }
            type=null;
            AudioWrapper.getInstance().destory();
            target_id = 0;
        }
    }
    @Subscriber(tag = "prepareTalk")
   synchronized private void prepareTalk(EventModal event) {
        if(event.getType().equals("prepare")) {
            if (prepareRecoard)
                return;
            if(!talk_wait) {
                Toast.makeText(this, res.getString(R.string.line_busy), Toast.LENGTH_SHORT).show();
                return;
            }
            prepareRecoard=true;
            UDPService.getInstence().prepareTalk();
        }else if(event.getType().equals("back")){
            boolean status=(boolean)event.getData();
            if(status){
                if(prepareRecoard)
                    mediaPlayer.start();
                else{
                    System.out.println("已经停止说话");
                }
            }else{
                Toast.makeText(this,res.getString(R.string.line_busy),Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Subscriber(tag = "member_talk")
    private void member_talk(EventModal event) {
        talk_wait=false;//此时已经有人在说话
    }
    @Subscriber(tag = "stopTalk")
    private void stopTalk(EventModal event){
        AudioWrapper.getInstance().stopRecord(false);
        UDPService.getInstence().stopPrepare();
        prepareRecoard = false;
        if(type!=null&&type.equals("single")) {
            SocketModel data = new SocketModel();
            data.setClientID(user.getId());
            data.setGroup_id(target_id);
            data.setType("stop_talk_single");
            WebSocketConnection.getInstence().sendMessage(data);
        }
    }
    @Subscriber(tag = "websocket_stop_talk")
    private void websocket_stop_talk(EventModal event){//从服务器收到停止说话消息
        talk_wait=true;
    }
    @Subscriber(tag = "RecorderBack")
    private void MediaRecorder(EventModal event){//语音文件录制完成
        long time=Long.parseLong(event.getType());
        if(time/1000==0) {//小于1秒的不算
            File file=new File(event.getData().toString());
            if (file.exists()){
                file.delete();
            }
            return;
        }
        if(target_id==0)
            return;
        if(type.equals("group")) {
            if (commonUtils.getConversation(target_id, "group",user.getId()) == null) {//检查有没会话存在，没有就创建会话
                Conversation conversation = new Conversation();
                conversation.setType("group");
                conversation.setUser_id(user.getId());
                conversation.setTarget_id(target_id);
        //            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                if (commonUtils.addConversation(conversation)) {
        //                System.out.println("添加会话列表完成");
                }

                EventModal eventModal=new EventModal();
                eventModal.setData(conversation);
                eventModal.setType("add");
                EventBus.getDefault().post(eventModal, "updateConversation");
            }
            Voice_Message group_message = new Voice_Message();
            group_message.setRead(true);
            group_message.setOwner(user.getId());
            group_message.setType(1);
            group_message.setTarget_id(target_id);
            group_message.setUser_id(user.getId());
            group_message.setVoice_length((int) time);
            group_message.setVoice_path(event.getData().toString());
            Date date_time=new Date();
            group_message.setDate_time(date_time);
            commonUtils.addVoiceMessage(group_message);
            VoiceData voiceData = new VoiceData();
            voiceData.setData(MyUtil.fileToByte(event.getData().toString()));
            voiceData.setClientID(user.getId());
            voiceData.setGroupID(target_id);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("clientID", String.valueOf(user.getId()));
            parameters.put("groupID", String.valueOf(target_id));
            parameters.put("time", String.valueOf(time));
            parameters.put("date_time", String.valueOf(date_time.getTime()));
            httpUtil.postAsynHttp(URLConfig.getUplaodVoice(), parameters, new File(event.getData().toString()), null);
        }else if(type.equals("single")){
            if (commonUtils.getConversation(target_id, "single",user.getId()) == null) {//检查有没会话存在，没有就创建会话
                Conversation conversation = new Conversation();
                conversation.setType("single");
                conversation.setUser_id(user.getId());
                conversation.setTarget_id(target_id);
                commonUtils.addConversation(conversation);
                EventModal eventModal=new EventModal();
                eventModal.setData(conversation);
                eventModal.setType("add");
                EventBus.getDefault().post(eventModal, "updateConversation");
            }
            Voice_Message group_message = new Voice_Message();
            group_message.setRead(true);
            group_message.setOwner(user.getId());
            group_message.setType(2);
            group_message.setTarget_id(target_id);
            group_message.setUser_id(user.getId());
            group_message.setVoice_length((int) time);
            group_message.setVoice_path(event.getData().toString());
            Date date_time=new Date();
            group_message.setDate_time(date_time);
            commonUtils.addVoiceMessage(group_message);
            EventModal eventModal=new EventModal();
            eventModal.setData(group_message);
            EventBus.getDefault().post(eventModal, "voiceData");
            VoiceData voiceData = new VoiceData();
            voiceData.setData(MyUtil.fileToByte(event.getData().toString()));
            voiceData.setClientID(user.getId());
            voiceData.setGroupID(target_id);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("clientID", String.valueOf(user.getId()));
            parameters.put("targetID", String.valueOf(target_id));
            parameters.put("time", String.valueOf(time));
            parameters.put("date_time", String.valueOf(date_time.getTime()));
            httpUtil.postAsynHttp(URLConfig.getUplaodVoiceSingle(), parameters, new File(event.getData().toString()), null);
        }
    }
    @Subscriber(tag = "addUserToGroup")//后台添加用户到群组
    private void addUserToGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        User user=JSON.parseObject(data.getData().toString(),User.class);
        for(Group group:groups){
            if(group.getId()==data.getGroup_id()){
                group.getMambers().add(user);
            }
        }
        data.setType("getUserIsOnline");
        webSocketConnection.sendMessage(data);
    }
    @Subscriber(tag = "get_voice")//收到后台语音数据消息
    private void get_voice(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        JSONObject json=JSON.parseObject(data.getData().toString());
        Map<String,String> parameters=new HashMap<>();
        parameters.put("path",json.getString("path"));
        parameters.put("clientID",String.valueOf(data.getClientID()));
        parameters.put("groupID",String.valueOf(data.getGroup_id()));
        parameters.put("type",data.getType());
        parameters.put("date_time",json.getString("date_time"));
        System.out.println("开始下载");
        httpUtil.postAsynHttp(URLConfig.getVoice(),parameters,this);
    }
    @Subscriber(tag = "setGroups")//获取所有群组
    private void setGroups(EventModal event) {
        groups=(List<Group>)event.getData();
//        System.out.println(JSON.toJSONString(groups));
        System.out.println("设置群组用户size:"+groups.size());
    }
    @Subscriber(tag = "addNewUser")//后台添加新用户
    private void addNewUser(EventModal event) {
        groups.get(groups.size()-1).getMambers().add((User)event.getData());
    }
    @Subscriber(tag = "deleteUser")//后台删除新用户
    private void deleteUser(EventModal event) {
        int userID=(Integer) event.getData();
        for(Group group:groups){
            for(User user:group.getMambers()){
                if(user.getId()==userID){
                    group.getMambers().remove(user);
                    break;
                }
            }
        }
    }
    @Subscriber(tag = "addNewGroup")//后台添加群组
    private void addNewGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        Group group=new Group();
        group.setId(data.getGroup_id());
        group.setName(data.getData().toString());
        group.setMambers(new ArrayList<User>());
        groups.add(groups.size()-1,group);
    }
    @Subscriber(tag = "deleteGroup")//后台添加群组
    private void deleteGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        for(Group group:groups){
            if(group.getId()==data.getGroup_id()){
                groups.remove(group);
                break;
            }
        }
    }
    @Subscriber(tag = "UDP_single")//单聊UDP消息相关
    private void UDP_init_single(EventModal event) {
        System.out.println("UDP_single---------------------------->here");
        if(event.getType().equals("UDP_init_single_back")){//UDP初始化返回，对方没有打开和自己的聊天窗口
            AudioEncoder.getInstance().setSingle(true);
            AudioWrapper.getInstance().InitOK();
        }else if(event.getType().equals("goaway_single")){
            SocketModel  data=(SocketModel)event.getData();
            System.out.println("单聊对象离开:"+data.getClientID());
        }else if(event.getType().equals("start_talk_single")){
            SocketModel  data=(SocketModel)event.getData();
            if(type!=null&&type.equals("single")){//判断当前处在单聊状态
                if(target_id==data.getClientID()){
                    talk_wait=false;
                }
            }
        }else if(event.getType().equals("stop_talk_single")){
            SocketModel  data=(SocketModel)event.getData();
            if(type!=null&&type.equals("single")){//判断当前处在单聊状态
                if(target_id==data.getClientID()){
                    talk_wait=true;
                }
            }
        }
    }
    @Subscriber(tag = "logout")//当前账号在别的地方登录
    private void logout(EventModal event) {
        onDestroy();
        AudioWrapper.getInstance().destory();
        Toast.makeText(this,res.getString(R.string.login_other),Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Subscriber(tag = "chang_talk")//单聊界面改变聊天对象
    private void chang_talk(EventModal event) {
        if(event.getType()!=null&&event.getType().equals("chang_talk_back")){//切换群组聊天返回端口号
            AudioWrapper.getInstance().initUDP("init_group",user.getId(), target_id,Integer.parseInt(event.getData().toString()));
        }else {
            JSONObject json = (JSONObject) event.getData();
            SocketModel socketModel = new SocketModel();
            socketModel.setClientID(user.getId());
            socketModel.setData(json);
            socketModel.setType("chang_talk");
            webSocketConnection.sendMessage(socketModel);
            target_id = json.getIntValue("new_id");
            if(json.getString("type").equals(type)&&type.equals("group")){//两个群组间切换
                AudioWrapper.getInstance().destory();
            }
        }
    }
    @Subscriber(tag = "websocket_close")//连接意外关闭
    private void websocket_close(EventModal event) {
        onDestroy();
        AudioWrapper.getInstance().destory();
        Toast.makeText(this,res.getString(R.string.connect_close),Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        webSocketConnection.disconnect();
        try {
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){}
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            user = intent.getParcelableExtra("user");
            if (user != null)
                webSocketConnection.connect(user.getId());
            else {
                Log.e("cmd", "WebSocketService启动失败，user为空");
            }
        }else{
            System.out.println("intent为空 结束");
            onDestroy();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSuccess(ReturnData responseBody) {
        if(responseBody.isStatus()){
            EventModal eventModal=new EventModal();
            VoiceData voiceData=JSON.parseObject(responseBody.getData().toString(),VoiceData.class);
            String file_path = path + "/" + UUID.randomUUID().toString() + ".amr";
            MyUtil.getFileFromBytes(voiceData.getData(), file_path);
            Voice_Message voice_message= new Voice_Message();
            voice_message.setVoice_length((int) voiceData.getTime());
            voice_message.setVoice_path(file_path);
            voice_message.setDate_time(voiceData.getDate_time());
            voice_message.setOwner(user.getId());
            if(voiceData.getType().equals("group_voice")) {
                voice_message.setTarget_id(voiceData.getGroupID());
                voice_message.setUser_id(voiceData.getClientID());
                voice_message.setType(1);
                if (commonUtils.getConversation(voiceData.getGroupID(), "group",user.getId()) == null) {//检查有没会话存在，没有就创建会话
                    Conversation conversation = new Conversation();
                    conversation.setType("group");
                    conversation.setUser_id(user.getId());
                    conversation.setTarget_id(voiceData.getGroupID());
                    commonUtils.addConversation(conversation);
                    eventModal.setData(conversation);
                    eventModal.setType("add");
                    EventBus.getDefault().post(eventModal, "updateConversation");
                }
            }else if(voiceData.getType().equals("single_voice")){
                voice_message.setTarget_id(user.getId());
                voice_message.setUser_id(voiceData.getClientID());
                System.out.println("Client_ID:"+voiceData.getGroupID());
                if (commonUtils.getConversation(voiceData.getClientID(), "single",user.getId()) == null) {//检查有没会话存在，没有就创建会话
                    Conversation conversation = new Conversation();
                    conversation.setType("single");
                    conversation.setUser_id(user.getId());
                    conversation.setTarget_id(voiceData.getClientID());
                    System.out.println(JSON.toJSONString(conversation));
                    commonUtils.addConversation(conversation);
                    eventModal.setData(conversation);
                    eventModal.setType("add");
                    EventBus.getDefault().post(eventModal, "updateConversation");
                }
                voice_message.setType(2);
            }else{
                System.out.println("voiceData.getType():"+voiceData.getType());
            }
            commonUtils.addVoiceMessage(voice_message);
            if(type!=null&&((type.equals("group")&&target_id ==voiceData.getGroupID()&&voiceData.getType().equals("group_voice"))||(type.equals("single")&&target_id==voiceData.getClientID()&&voiceData.getType().equals("single_voice")))) {//如果是当前所在分组的语音
                eventModal.setData(voice_message);
                EventBus.getDefault().post(eventModal, "voiceData");
            }else{//发送通知
                showNotification(voiceData);
                eventModal.setType("refresh");
                EventBus.getDefault().post(eventModal, "updateConversation");
            }
        }else{
            Message message=new Message();
            message.what=http_false;
            message.obj=responseBody.getMessage();
            myHandler.sendMessage(message);
        }
    }

    @Override
    public void onFailure(String message) {
        Message msg=new Message();
        msg.what=http_false;
        msg.obj=message;
        myHandler.sendMessage(msg);
    }
    public void showNotification(VoiceData voiceData) {
//        System.out.println("显示通知");
        //得到NotificationManager的对象，用来实现发送Notification
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(this);
        if(voiceData.getType().equals("group_voice")) {
            Intent intent = new Intent(getApplication(), GroupTalkActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("user",user);
            mBundle.putParcelable("group",getGroup(voiceData.getGroupID()));
            intent.putExtras(mBundle);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent).
                    setSmallIcon(R.mipmap.ic_launcher)//设置状态栏里面的图标（小图标） 　　　　　　　　　　　　　　　　　　　　
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//下拉下拉列表里面的图标（大图标） 　　　　　　　
                    .setWhen(System.currentTimeMillis())//设置时间发生时间
                    .setAutoCancel(true);//设置可以清除

            if(isChina){
                builder.setTicker("群组（" + getGroupName(voiceData.getGroupID()) + "）中 " + getUser(voiceData.getClientID()).getUsername() + "发来消息")//设置状态栏的显示的信息
                         .setContentTitle(getUser(voiceData.getClientID()).getUsername() + "发来消息")//设置下拉列表里的标题
                        .setContentText("群组（" + getGroupName(voiceData.getGroupID()) + "）中 " + getUser(voiceData.getClientID()).getUsername() + "发来消息");//设置上下文内容
            }else{
                builder.setTicker("get message from" +getUser(voiceData.getClientID()).getUsername()+" of group("+ getGroupName(voiceData.getGroupID()) + "）")//设置状态栏的显示的信息
                        .setContentTitle("get message from" +getUser(voiceData.getClientID()).getUsername())//设置下拉列表里的标题
                        .setContentText("get message from" +getUser(voiceData.getClientID()).getUsername()+" of group("+ getGroupName(voiceData.getGroupID()) + "）");//设置上下文内容
            }
        }else if(voiceData.getType().equals("single_voice")){
            Intent intent = new Intent(getApplication(), OneToOneTalkActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("currentUser",user);
            User target=getUser(voiceData.getClientID());
//            System.out.println("target_id:"+voiceData.getClientID());
//            System.out.println(JSON.toJSONString(target));
//            System.out.println("单聊消息，聊天对象："+target.getUsername());
            mBundle.putParcelable("targetUser",target);
            intent.putExtras(mBundle);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent).
                    setSmallIcon(R.mipmap.ic_launcher)//设置状态栏里面的图标（小图标） 　　　　　　　　　　　　　　　　　　　　
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//下拉下拉列表里面的图标（大图标） 　　　　　　　

                    .setWhen(System.currentTimeMillis())//设置时间发生时间
                    .setAutoCancel(true);//设置可以清除

            if(isChina){
                builder.setTicker(target.getUsername() + "发来消息")//设置状态栏的显示的信息
                        .setContentTitle(target.getUsername() + "发来消息")//设置下拉列表里的标题
                        .setContentText(target.getUsername() + "发来消息");//设置上下文内容
            }else{
                builder.setTicker("get message from" +target.getUsername())//设置状态栏的显示的信息
                        .setContentTitle("get message from" +target.getUsername())//设置下拉列表里的标题
                        .setContentText("get message from" +target.getUsername());//设置上下文内容
            }
        }
        //得到Notification对象
        Notification notification = builder.build();
        notification.defaults=Notification.DEFAULT_SOUND;//设置为默认的声音
        //启动Notification
        manager.notify(110, notification);
        //取消通知
        //manager.cancelAll();
    }
    private String getGroupName(int groupID){
        for(Group group:groups){
            if(group.getId()==groupID){
                return group.getName();
            }
        }
        return "null";
    }
    private Group getGroup(int groupID){
        for(Group group:groups){
            if(group.getId()==groupID){
                return group;
            }
        }
        return null;
    }
    private User getUser(int userID){
        for(User user:groups.get(groups.size()-1).getMambers()){
            if(user.getId()==userID){
                return user;
            }
        }
        return null;
    }

}
