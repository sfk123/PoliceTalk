package com.sheng.android.policetalk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.activity.LoginActivity;
import com.sheng.android.policetalk.activity.GroupTalkActivity;
import com.sheng.android.policetalk.activity.OneToOneTalkActivity;
import com.sheng.android.policetalk.adapter.IdeasExpandableListAdapter;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.User;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/20.
 */

public class FragmentContacts extends Fragment implements ExpandableListView.OnChildClickListener{
    @Bind(R.id.mlist)
    public ExpandableListView mlist;

    private User user;
    private List<Group> groups;
    private IdeasExpandableListAdapter adapter;
    @Subscriber(tag = "changeLanguage")//当前账号在别的地方登录
    private void changeLanguage(EventModal event) {
        if(adapter!=null){
            adapter.changeLanguage();
        }
    }
    public void setGroups(List<Group> groups){
        this.groups=groups;
        sortUsers();
        adapter=new IdeasExpandableListAdapter(getContext(),groups);
        mlist.setAdapter(adapter);
        for(int i = 0; i < adapter.getGroupCount(); i++){
            mlist.expandGroup(i);
        }
    }
    private void sortUsers(){
        Collections.sort(groups.get(groups.size() - 1).getMambers(), new Comparator<User>() {
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
    public void setUser(User user){
        this.user=user;
    }
    public void UserOnline(int userID){
        for(Group group:groups){
            for(User user:group.getMambers()){
                if(user.getId()==userID){
                    user.setOnline(true);
                }
            }
        }
        for(User user:adapter.getUsers()){
            if(user.getId()==userID){
                user.setOnline(true);
                break;
            }
        }
        sortUsers();
        adapter.notifyDataSetChanged();
    }
    public void UserOffline(int userID){
        for(Group group:groups){
            for(User user:group.getMambers()){
                if(user.getId()==userID){
                    user.setOnline(false);
                }
            }
        }
        for(User user:adapter.getUsers()){
            if(user.getId()==userID){
                user.setOnline(false);
                break;
            }
        }
        sortUsers();
        adapter.notifyDataSetChanged();
    }
    public void addNewUser(User user){//后台添加新用户
        System.out.println("添加新用户");
        groups.get(groups.size()-1).getMambers().add(user);
        sortUsers();
        adapter.notifyDataSetChanged();
    }
    public void deleteUser(int user_id){
        for(Group group:groups){
            for(User user:group.getMambers()){
                if(user.getId()==user_id){
                    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<删除联系人");
                    group.getMambers().remove(user);
                    break;
                }
            }
        }
        sortUsers();
        adapter.notifyDataSetChanged();
    }
    public void addUserToGroup(int user_id,int group_id){
//        User user=null;
//        for(User u:groups.get(groups.size()-1).getMambers()){
//            if(u.getId()==user_id){
//                user=u;
//            }
//        }
//        if(user!=null) {
//            for (Group group : groups) {
//                if (group.getId() == group_id) {
////                    group.getMambers().add(user);
//                    System.out.println("添加后数量："+group.getMambers().size());
//                    break;
//                }
//            }
//        }else{
//            Toast.makeText(getContext(),"addUserToGroup:没有找到用户",Toast.LENGTH_SHORT).show();
//        }
        adapter.notifyDataSetChanged();
    }
    public void removeUserOfGroup(int user_id,int group_id){
        for (Group group : groups) {
            if (group.getId() == group_id) {
                for(User user:group.getMambers()){
                    if(user.getId()==user_id) {
                        System.out.println("删除群组成员");
                        group.getMambers().remove(user);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    public void addGroup(Group group){
        groups.add(groups.size()-1,group);
        adapter.notifyDataSetChanged();
    }
    public void deleteGroup(int groupID){
        for(Group group:groups){
            if(group.getId()==groupID){
                groups.remove(group);
                break;
            }
        }
        adapter.notifyDataSetChanged();
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HeadlinesFragment", "onCreate");
        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        EventBus.getDefault().register(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("HeadlinesFragment", "onCreateView");
        View view = inflater.inflate(R.layout.layout_contacts, container, false);
        ButterKnife.bind(this,view);
        mlist.setOnChildClickListener(this);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Object item=adapter.getChild(groupPosition,childPosition);
        if(item instanceof Group){//打开群聊界面
            Group group=(Group)item;
            boolean contain=false;
            for(User u:group.getMambers()){
                if(u.getId()==user.getId()){
                    contain=true;
                }
            }
            if(!contain){
                Toast.makeText(getContext(),"你不在当前分组中",Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(getContext(), GroupTalkActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("group", group);
                mBundle.putParcelable("user", user);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        }else{//打开单聊界面
            User user=(User)item;
            if(user.getOnline()) {
                if (user.getId() != this.user.getId()) {
                    Intent intent = new Intent(getContext(), OneToOneTalkActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable("currentUser", this.user);
                    mBundle.putParcelable("targetUser", user);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "不能选择自己", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getContext(), "对方不在线，不可发起对话", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
    @Override
    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
