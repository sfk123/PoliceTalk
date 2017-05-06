package com.sheng.android.policetalk.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.WebSocket.WebSocketConnection;
import com.sheng.android.policetalk.adapter.MyFragmentPagerAdapter;
import com.sheng.android.policetalk.fragment.FragmentContacts;
import com.sheng.android.policetalk.fragment.FragmentMessage;
import com.sheng.android.policetalk.fragment.FragmentSettings;
import com.sheng.android.policetalk.listener.MyOnPageChangeListener;
import com.sheng.android.policetalk.listener.PageChange;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.SocketModel;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.service.WebSocketService;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.util.MyUtil;
import com.sheng.android.policetalk.view.CustomViewPager;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PageChange,HttpCallBack {

    @Bind(R.id.viewpager)
    public CustomViewPager viewpager;
    @Bind(R.id.img_message)
    ImageView img_message;
    @Bind(R.id.img_contacts)
    ImageView img_contacts;
    @Bind(R.id.img_settings)
    ImageView img_settings;
    @Bind(R.id.tv_settings)
    TextView tv_settings;
    @Bind(R.id.tv_contacts)
    TextView tv_contacts;
    @Bind(R.id.tv_message)
    TextView tv_message;
    TextView tv_name;
    private RoundImageView img_back;
    private TextView tv_title;
    private ArrayList<Fragment> fragmentList;
    private FragmentContacts fragmentContacts;
    private FragmentMessage fragmentMessage;
    private FragmentSettings fragmentSettings;

    private Resources res;
    private User user;
    private Boolean initOk=false;
    private ProgressDialog myDialog;
    private Boolean first=true;
    private List<Integer> onlineIDs;
    private HttpUtil httpUtil;
    private List<Group> groups;
    private final int connect_timeout=1001;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case connect_timeout:
                    if(myDialog!=null&&myDialog.isShowing()){
                        try {
                            myDialog.dismiss();
                        }catch (Exception e){}
                    }
                    Toast.makeText(MainActivity.this,res.getText(R.string.connect_time_out),Toast.LENGTH_SHORT).show();
                    Intent startIntent = new Intent(MainActivity.this, WebSocketService.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable("user",user);
                    startIntent.putExtras(mBundle);
                    stopService(startIntent);
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(first){
            if(!initOk) {
                if(myDialog==null) {
                    myDialog = ProgressDialog.show(this, res.getText(R.string.connecting), res.getText(R.string.connect_wait), true, true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(30000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(!initOk) {
                                Message message = new Message();
                                message.what = connect_timeout;
                                myHandler.sendMessage(message);
                            }
                        }
                    }).start();
                }
                myDialog.setCancelable(false);
            }
            first = false;
        }
    }
    @Subscriber(tag = "connect")
    private void connectEvent(EventModal event) {
        if(event.getType().equals("connect")){
            onlineIDs= JSON.parseArray(event.getData().toString(),Integer.class);
            httpUtil.postAsynHttp(URLConfig.getContactsUrl(),new HashMap<String, String>(),this);
        }
    }
    @Subscriber(tag = "userEvent")
    private void userEvent(EventModal event) {
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<userEvent");
        if(event.getType().equals("changePhoto")){//用户更改头像
            SocketModel data=(SocketModel)event.getData();
            User user_temp=JSON.parseObject(data.getData().toString(),User.class);
            for(Group group:groups){
                for(User user:group.getMambers()){
                    if(user.getId()==user_temp.getId()){
                        user.setPhoto(user_temp.getPhoto());
                    }
                }
            }
            fragmentContacts.UserChangePhoto(user_temp);
            fragmentMessage.UserChangePhoto(user_temp);
        }else if(event.getType().equals("changePhoto_local")){//本地用户更改头像
            user.setPhoto(event.getData().toString());
            fragmentContacts.UserChangePhoto(user);
            fragmentMessage.UserChangePhoto(user);
        }else if(event.getType().equals("removeUserOfGroup")){//踢出群组成员
            JSONObject json=(JSONObject)event.getData();
            int group_id=json.getIntValue("group_id");
            int user_id=json.getIntValue("user_id");
            for(Group group:groups){
                if(group.getId()==group_id) {
                    for (User user : group.getMambers()) {
                        if (user.getId() ==user_id) {
                            group.getMambers().remove(user);
                        }
                    }
                }
            }
            fragmentContacts.removeUserOfGroup(user_id,group_id);
        }
    }
    @Subscriber(tag = "group_event")
    private void group_event(EventModal event) {
        if(event.getType().equals("addGroupManager")){
            SocketModel socketModel=(SocketModel)event.getData();
            User user1=JSON.parseObject(socketModel.getData().toString(),User.class);
            for(Group group:groups){
                if(group.getId()==socketModel.getGroup_id()){
                    group.getManagers().add(user1);
                }
            }
        }
    }
    @Subscriber(tag = "online")
    private void Useronline(final EventModal event) {
        /*******兼容本地acitvity刚打开，还没有获取到user列表时，收到用户上线的消息********/
        if(!initOk) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!initOk){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int userID = Integer.parseInt(event.getData().toString());
                            fragmentContacts.UserOnline(userID);
                            fragmentMessage.UserOnline(userID);
                        }
                    });
                }
            }).start();
        }else {
            int userID = Integer.parseInt(event.getData().toString());
            fragmentContacts.UserOnline(userID);
            fragmentMessage.UserOnline(userID);
        }
    }
    @Subscriber(tag = "offline")
    private void Useroffline(EventModal event) {
        int userID=Integer.parseInt(event.getData().toString());
        fragmentContacts.UserOffline(userID);
        fragmentMessage.UserOffline(userID);
    }
    @Subscriber(tag = "getGroups")
    private void getGroups(EventModal event) {
        EventModal eventModal=new EventModal();
        eventModal.setData(groups);
        EventBus.getDefault().post(eventModal, event.getType());
    }
    @Subscriber(tag = "addNewUser")//后台添加新用户
    private void addNewUser(EventModal event) {
       User user=(User)event.getData();
        fragmentContacts.addNewUser(user);
        fragmentMessage.addNewUser(user);
    }
    @Subscriber(tag = "deleteUser")//后台删除用户
    private void deleteUser(EventModal event) {
        int user_id=(int)event.getData();
        if(user_id==user.getId()){//如果删除的是当前用户直接退出到登录界面
            Toast.makeText(getApplicationContext(),res.getText(R.string.current_user_delete),Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            //删除单用户会话列表

            //删除联系人
            fragmentContacts.deleteUser(user_id);
            fragmentMessage.deleteUser(user_id);
        }
    }
    @Subscriber(tag = "addUserToGroup")//后台添加用户到群组
    private void addUserToGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        fragmentContacts.addUserToGroup(data.getClientID(),data.getGroup_id());
    }
    @Subscriber(tag = "removeUserOfGroup")//后台剔除群组成员
    private void removeUserOfGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        fragmentContacts.removeUserOfGroup(data.getClientID(),data.getGroup_id());
    }
    @Subscriber(tag = "updateConversation")//更新会话列表
    private void updateConversation(EventModal event) {
        System.out.println("updateConversation<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        if(event.getType().equals("add")) {
            Conversation conversation = (Conversation) event.getData();
            fragmentMessage.addConversation(conversation);
        }else if(event.getType().equals("refresh")){
            fragmentMessage.refreshConversation();
        }
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
        fragmentContacts.deleteGroup(data.getGroup_id());
    }
    @Subscriber(tag = "addNewGroup")//后台添加群组
    private void addNewGroup(EventModal event) {
        SocketModel  data=(SocketModel)event.getData();
        Group group=new Group();
        group.setId(data.getGroup_id());
        group.setName(data.getData().toString());
        group.setMambers(new ArrayList<User>());
        groups.add(groups.size()-1,group);
        fragmentContacts.addGroup(group);
    }
    @Subscriber(tag = "logout")//当前账号在别的地方登录
    private void logout(EventModal event) {
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
        }catch (Exception e){
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        ButterKnife.bind(this);
        user=getIntent().getParcelableExtra("user");
        fragmentList = new ArrayList<>();
        fragmentMessage=new FragmentMessage();
        fragmentMessage.setUser(user);
        fragmentList.add(fragmentMessage);
        fragmentContacts=new FragmentContacts();
        fragmentContacts.setUser(user);
        fragmentList.add(fragmentContacts);
        fragmentSettings=new FragmentSettings();
        fragmentList.add(fragmentSettings);
        viewpager.setOnPageChangeListener(new MyOnPageChangeListener(this));
        viewpager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        viewpager.setCurrentItem(0);//设置当前显示标签页为第一页
        EventBus.getDefault().register(this);
        httpUtil=HttpUtil.getInstance();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        img_back=(RoundImageView) actionBar.getCustomView().findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        tv_name=(TextView)actionBar.getCustomView().findViewById(R.id.tv_name);
        tv_title=(TextView)actionBar.getCustomView().findViewById(R.id.tv_title);

        changeLanguage(null);
    }
    @Subscriber(tag = "changeLanguage")//当前账号在别的地方登录
    private void changeLanguage(EventModal event) {
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
        setString();
    }
    private void setString(){
        if(viewpager.getCurrentItem()==0)
            tv_title.setText(res.getText(R.string.message));
        else if(viewpager.getCurrentItem()==1)
            tv_title.setText(res.getText(R.string.contacts));
        else
            tv_title.setText(res.getText(R.string.setting));
        tv_message.setText(res.getText(R.string.message));
        tv_contacts.setText(res.getText(R.string.contacts));
        tv_settings.setText(res.getText(R.string.setting));
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(MyUtil.isNetworkConnected(this)) {
            if(!MyUtil.isServiceRunning(this,WebSocketService.class.getName())) {
                Intent startIntent = new Intent(this, WebSocketService.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("user",user);
                startIntent.putExtras(mBundle);
                startService(startIntent);
            }else if(!initOk){
                SocketModel data = new SocketModel();
                data.setClientID(user.getId());
                data.setType("getOnlines");//发送命令，获取在线用户
                WebSocketConnection.getInstence().sendMessage(data);
            }
        }else{
            new AlertDialog.Builder(MainActivity.this).setTitle(res.getText(R.string.sys_notice))//设置对话框标题
                    .setMessage(res.getText(R.string.check_network))//设置显示的内容
                    .setPositiveButton(res.getText(R.string.btn_determine),new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            finish();
                        }

                    }).show();
        }
    }
    @Override
    protected void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onPageSelected(int currentIndex) {
        if(currentIndex==0){
            img_back.setVisibility(View.GONE);
            tv_name.setVisibility(View.GONE);
            tv_title.setText(res.getText(R.string.message));
            img_message.setImageResource(R.mipmap.message_on);
            tv_message.setTextColor(getResources().getColor(R.color.button_color));
            img_contacts.setImageResource(R.mipmap.contacts);
            tv_contacts.setTextColor(getResources().getColor(R.color.text_normal));
            img_settings.setImageResource(R.mipmap.setting);
            tv_settings.setTextColor(getResources().getColor(R.color.text_normal));
            viewpager.setScanScroll(false);
        }else if(currentIndex==1){
            tv_title.setText(res.getText(R.string.contacts));
            img_message.setImageResource(R.mipmap.message);
            tv_message.setTextColor(getResources().getColor(R.color.text_normal));
            img_contacts.setImageResource(R.mipmap.contacts_on);
            tv_contacts.setTextColor(getResources().getColor(R.color.button_color));
            img_settings.setImageResource(R.mipmap.setting);
            tv_settings.setTextColor(getResources().getColor(R.color.text_normal));
            viewpager.setScanScroll(true);
            tv_name.setText(user.getUsername());
            tv_name.setVisibility(View.VISIBLE);
            img_back.setVisibility(View.VISIBLE);
            if(user.getPhoto()==null||user.getPhoto().equals("")) {
                img_back.setImageResource(R.mipmap.head_online);
            }else{
                Picasso.with(this).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(img_back);
            }
        }else if(currentIndex==2){
            img_back.setVisibility(View.GONE);
            tv_name.setVisibility(View.GONE);
            tv_title.setText(res.getText(R.string.setting));
            img_message.setImageResource(R.mipmap.message);
            tv_message.setTextColor(getResources().getColor(R.color.text_normal));
            img_contacts.setImageResource(R.mipmap.contacts);
            tv_contacts.setTextColor(getResources().getColor(R.color.text_normal));
            img_settings.setImageResource(R.mipmap.setting_on);
            tv_settings.setTextColor(getResources().getColor(R.color.button_color));
            fragmentSettings.setUser(user);
            viewpager.setScanScroll(true);
        }
    }
    @OnClick(R.id.tab_message)
    public void messageClick(){
        viewpager.setCurrentItem(0);
    }
    @OnClick(R.id.tab_contacts)
    public void contactsClick(){
        viewpager.setCurrentItem(1);
    }
    @OnClick(R.id.tab_settings)
    public void settingsClick(){
        viewpager.setCurrentItem(2);
    }


    @Override
    public void onSuccess(final ReturnData responseBody) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(myDialog!=null&&myDialog.isShowing()){
                        myDialog.dismiss();
                    }
                    if(responseBody.isStatus()){
                       groups= JSON.parseArray(responseBody.getData().toString(),Group.class);
                        for(Group group:groups){
                            for(User user:group.getMambers()){
                                if(onlineIDs.contains(user.getId())){
                                    user.setOnline(true);
                                }
                            }
                        }
                        for(User user:groups.get(groups.size()-1).getMambers()){
                            if(onlineIDs.contains(user.getId())){
                                user.setOnline(true);
                            }
                        }
                        onlineIDs.clear();
                        onlineIDs=null;
                        if(!isFinishing()) {
                            fragmentContacts.setGroups(groups);
                            fragmentMessage.setGroups(groups);
                            EventModal eventModal = new EventModal();
                            eventModal.setData(groups);
                            EventBus.getDefault().post(eventModal, "setGroups");
                        }
                    }else {
                        if(responseBody.getMessage().equals("用户未登录，试图访问！")){
                            Toast.makeText(getApplicationContext(), res.getText(R.string.login_expired), Toast.LENGTH_SHORT).show();
                            Intent startIntent = new Intent(getApplication(), WebSocketService.class);
                            stopService(startIntent);
                            WebSocketConnection.getInstence().disconnect();
                           Intent intent=new Intent(getApplication(),LoginActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(), responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                    initOk=true;
                }
        });
//        Message msg=new Message();
//        if(responseBody.isStatus()) {
//            msg.what = http_back_success;
//            msg.obj = responseBody.getData().toString();
//        }else{
//            msg.what = http_back_fail;
//            msg.obj = responseBody.getMessage();
//        }
//        myHandler.sendMessage(msg);
    }

    @Override
    public void onFailure(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(myDialog!=null&&myDialog.isShowing())
                    myDialog.dismiss();
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
