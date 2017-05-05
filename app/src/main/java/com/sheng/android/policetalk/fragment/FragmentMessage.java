package com.sheng.android.policetalk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.activity.GroupTalkActivity;
import com.sheng.android.policetalk.activity.OneToOneTalkActivity;
import com.sheng.android.policetalk.adapter.ConversationsAdapter;
import com.sheng.android.policetalk.adapter.GroupMemberAdapter;
import com.sheng.android.policetalk.adapter.RefreshRecyclerAdapter;
import com.sheng.android.policetalk.adapter.SwipeItemOnItemChildClickListener;
import com.sheng.android.policetalk.dao.CommonUtils;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.modal.Voice_Message;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.view.LoadingDialog;
import com.sheng.android.policetalk.view.MyDecoration;
import com.sheng.android.policetalk.view.PopupGroupMember;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;

/**
 * Created by Administrator on 2017/3/20.
 */

public class FragmentMessage extends Fragment implements View.OnClickListener,SwipeItemOnItemChildClickListener,RecyclerArrayAdapter.OnItemClickListener {
//    @Bind(R.id.swiperefreshlayout)
//    public SwipeRefreshLayout swiperefreshlayout;
    @Bind(R.id.recyclerview)
    public EasyRecyclerView recyclerview;

    private LinearLayoutManager linearLayoutManager;
    private ConversationsAdapter adapter;
    private CommonUtils commonUtils;
    private User user;
    private List<Group> groups;
    private List<Conversation> conversations;

    private Resources res;
    //数据保持
    private Bundle savedState;
    public FragmentMessage(){
        Bundle arguments = new Bundle();
        setArguments(arguments);
    }
    public void setUser(User user){
        this.user=user;
    }
    public void setGroups(List<Group> groups){
        this.groups=groups;
        initView();
    }
    private void initView(){
        conversations=commonUtils.getConversationList(user.getId());
        if(conversations==null){
            conversations=new ArrayList<>();
        }
        for(Conversation conversation:conversations){
            if(conversation.getType().equals("single"))
                conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),2));
            else
                conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),1));
        }
//        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        System.out.println("会话列表："+conversations.size());
//        System.out.println(JSON.toJSONString(conversations));
//        recycler.setAdapter(adapter = new RefreshRecyclerAdapter(getContext(),conversations,this,this));
        changeLanguage(null);

        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerview.setItemAnimator(new ScaleInBottomAnimator(new OvershootInterpolator(1f)));
        recyclerview.setAdapter(adapter = new ConversationsAdapter(getContext(),res,this));
        adapter.setSwipeItemOnItemChildClickListener(this);//设置侧滑菜单控件点击监听
        adapter.setOnItemClickListener(this);
        recyclerview.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    adapter.closeOpenedSwipeItemLayoutWithAnim();
                }
            }
        });
        recyclerview.addItemDecoration(new MyDecoration(getContext(), OrientationHelper.VERTICAL));
        adapter.addAll(conversations);
    }
    @Override
    public void onResume(){
        super.onResume();
        System.out.println("fragment_message onResume");
        if(conversations!=null){
            System.out.println("重新查询未读数量");
            for(Conversation conversation:conversations){
                if(conversation.getType().equals("single"))
                    conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),2));
                else
                    conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),1));
            }
            adapter.notifyDataSetChanged();
        }
    }
    public void addNewUser(User user){
        groups.get(groups.size()-1).getMambers().add(user);
    }
    public void deleteUser(int user_id){
        for(Group group:groups){
            for(User u:group.getMambers()){
                if(u.getId()==user_id){
                    group.getMambers().remove(u);
                    break;
                }
            }
        }
    }
    public void UserChangePhoto(User user_temp){
        for(Group group:groups){
            for(User user:group.getMambers()){
                if(user.getId()==user_temp.getId()){
                    user.setPhoto(user_temp.getPhoto());
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    public User getUser(int user_id){
        for(User user:groups.get(groups.size()-1).getMambers()){
            if(user.getId()==user_id){
                return user;
            }
        }

        return null;
    }
    public void UserOnline(int user_id){
        for(User user:groups.get(groups.size()-1).getMambers()){
            if(user.getId()==user_id){
                user.setOnline(true);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }
    public void UserOffline(int user_id){
        for(User user:groups.get(groups.size()-1).getMambers()){
            if(user.getId()==user_id){
                user.setOnline(false);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }
    public Group getGroup(int group_id){
        for(Group group:groups){
            if(group.getId()==group_id){
                return group;
            }
        }
        return null;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }
    @Subscriber(tag = "changeLanguage")//当前账号在别的地方登录
    private void changeLanguage(EventModal event) {
        if(getContext()!=null){
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("wujay", Context.MODE_PRIVATE);
            String language=sharedPreferences.getString("language","中文");
            Locale myLocale;
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
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Restore State Here
        if (!restoreStateFromArguments()) {
            // First Time running, Initialize something here
        }else{
            EventModal eventModal=new EventModal();
            eventModal.setType("FragmentMessage_getGroups");
            EventBus.getDefault().post(eventModal, "getGroups");
        }
    }
    @Subscriber(tag = "FragmentMessage_getGroups")
    private void FragmentMessage_getGroups(EventModal event) {
        this.groups=(List<Group>)event.getData();
        initView();
    }
    public void addConversation(Conversation conversation){
        if(conversation.getType().equals("single"))
            conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),2));
        else
            conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),1));
        adapter.add(conversation);

        System.out.println("更新视图--------------------------");
        adapter.notifyDataSetChanged();
    }
    public void refreshConversation(){
        conversations=commonUtils.getConversationList(user.getId());
        for(Conversation conversation:conversations){
            if(conversation.getType().equals("single"))
                conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),2));
            else
                conversation.setUnread_count(commonUtils.getUnredCountOfConversation(user.getId(), conversation.getTarget_id(),1));
        }
        adapter.notifyDataSetChanged();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("HeadlinesFragment", "onCreateView");
        View view = inflater.inflate(R.layout.layout_message, container, false);
        ButterKnife.bind(this,view);
        commonUtils=new CommonUtils(getContext());
//        swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                Log.d("zttjiangqq","invoke onRefresh...");
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        List<String> newDatas = new ArrayList<String>();
////                        for (int i = 0; i <5; i++) {
////                            int index = i + 1;
////                            newDatas.add("new item" + index);
////                        }
////                        adapter.addItem(newDatas);
//                        swiperefreshlayout.setRefreshing(false);
//                        Toast.makeText(getContext(), "更新了五条数据...", Toast.LENGTH_SHORT).show();
//                    }
//                }, 5000);
//            }
//        });

//        linearLayoutManager=new LinearLayoutManager(getContext());
//        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
//        recycler.setLayoutManager(linearLayoutManager);
//        //添加分隔线
//        recycler.addItemDecoration(new MyDecoration(getContext(), OrientationHelper.VERTICAL));

        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save State Here
        saveStateToArguments();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save State Here
        saveStateToArguments();
    }
    private void saveStateToArguments() {
        savedState = saveState();
        if (savedState != null) {
            Bundle b = getArguments();
            b.putBundle("internalSavedViewState8954201239547", savedState);
        }
    }
    //////////////////////////////
    // 保存状态数据
    //////////////////////////////
    private Bundle saveState() {
        Bundle state = new Bundle();
        // 比如
        //state.putString(“text”, tv1.getText().toString());
        return state;
    }
    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();
        if(b==null)
            return false;
        savedState = b.getBundle("internalSavedViewState8954201239547");
        if (savedState != null) {
            restoreState();
            return true;
        }
        return false;
    }
    /////////////////////////////////
    // 取出状态数据
    /////////////////////////////////
    private void restoreState() {
        if (savedState != null) {
            //比如
            //tv1.setText(savedState.getString(“text”));
        }
    }
    @Override
    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Conversation conversation=(Conversation)v.getTag();
        if(conversation.getType().equals("group")){
            Group group=getGroup(conversation.getTarget_id());
            Intent intent=new Intent(getContext(), GroupTalkActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("group",group);
            mBundle.putParcelable("user",user);
            intent.putExtras(mBundle);
            startActivity(intent);
        }else{
            User target=getUser(conversation.getTarget_id());
            if(target.getOnline()) {
                Intent intent = new Intent(getContext(), OneToOneTalkActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("currentUser", this.user);
                mBundle.putParcelable("targetUser", target);
                intent.putExtras(mBundle);
                startActivity(intent);
            }else{
                Toast.makeText(getContext(), res.getString(R.string.toast_target_not_online), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemChildClick(ViewGroup var1, View var2, int position) {
        final Conversation conversation=conversations.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // 设置显示信息
        builder.setMessage("确定要删除当前会话？")
                .
                // 设置确定按钮
                        setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            // 单击事件
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                commonUtils.deleteConversation(conversation);
                                adapter.remove(conversation);
                                adapter.notifyDataSetChanged();
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
    }

    @Override
    public boolean onItemChildLongClick(ViewGroup var1, View var2, int var3) {
        return false;
    }

    @Override
    public void onItemClick(int position) {
        Conversation conversation=conversations.get(position);
        if(conversation.getType().equals("group")){
            Group group=getGroup(conversation.getTarget_id());
            Intent intent=new Intent(getContext(), GroupTalkActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("group",group);
            mBundle.putParcelable("user",user);
            intent.putExtras(mBundle);
            startActivity(intent);
        }else{
            User target=getUser(conversation.getTarget_id());
            if(target.getOnline()) {
                Intent intent = new Intent(getContext(), OneToOneTalkActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("currentUser", this.user);
                mBundle.putParcelable("targetUser", target);
                intent.putExtras(mBundle);
                startActivity(intent);
            }else{
                Toast.makeText(getContext(), res.getString(R.string.toast_target_not_online), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
