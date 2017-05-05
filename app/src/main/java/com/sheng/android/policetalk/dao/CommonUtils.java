package com.sheng.android.policetalk.dao;

import android.content.Context;

import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.ConversationDao;
import com.sheng.android.policetalk.modal.DaoMaster;
import com.sheng.android.policetalk.modal.Voice_Message;
import com.sheng.android.policetalk.modal.Voice_MessageDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/21.
 */

public class CommonUtils {
    private DaoManager daoManager;

    //构造方法
    public CommonUtils(Context context) {
        daoManager = DaoManager.getInstance();
        daoManager.initManager(context);
    }

    /**
     * 对数据库中Group_Message表的插入操作
     * @param group_message
     * @return
     */
    public boolean addVoiceMessage(Voice_Message group_message) {
        boolean flag ;
        flag = daoManager.getDaoSession().insert(group_message) != -1 ? true : false;
        return flag;
    }
    public List<Voice_Message> getGroupMessage(int current_id,int target_id,int type,long firstid){
        QueryBuilder<Voice_Message> queryBuilder=daoManager.getDaoSession().queryBuilder(Voice_Message.class);
        queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Type.eq(type)).offset(0).limit(10).orderDesc(Voice_MessageDao.Properties.Date_time)
                .where(Voice_MessageDao.Properties.Owner.eq(current_id));
        if(type==1){//群聊
            queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Target_id.eq(target_id));
        }else if(type==2){//单聊
            queryBuilder=queryBuilder.where(queryBuilder.or(queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(target_id),Voice_MessageDao.Properties.User_id.eq(current_id)),
                    queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(current_id),Voice_MessageDao.Properties.User_id.eq(target_id))));
        }
        if(firstid!=0) {
            queryBuilder.where(Voice_MessageDao.Properties.Id.lt(firstid));
        }
        return queryBuilder.list();
    }
    public long getUnredCountOfConversation(int current_id,int target_id,int type){
        QueryBuilder<Voice_Message> queryBuilder=daoManager.getDaoSession().queryBuilder(Voice_Message.class);
        queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Type.eq(type)).offset(0).limit(10).orderDesc(Voice_MessageDao.Properties.Date_time).where(Voice_MessageDao.Properties.Read.eq(false)).where(Voice_MessageDao.Properties.User_id.notEq(current_id))
                .where(Voice_MessageDao.Properties.Owner.eq(current_id));
        if(type==1){//群聊
            queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Target_id.eq(target_id));
        }else if(type==2){//单聊
            queryBuilder=queryBuilder.where(queryBuilder.or(queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(target_id),Voice_MessageDao.Properties.User_id.eq(current_id)),queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(current_id),Voice_MessageDao.Properties.User_id.eq(target_id))));
        }
        return queryBuilder.buildCount().count();
    }
    public List<Voice_Message> getGroupMessage(int current_id,int target_id,int type){
        QueryBuilder<Voice_Message> queryBuilder=daoManager.getDaoSession().queryBuilder(Voice_Message.class);
        queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Type.eq(type)).orderDesc(Voice_MessageDao.Properties.Date_time)
                .where(Voice_MessageDao.Properties.Owner.eq(current_id));
        if(type==1){//群聊
            queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Target_id.eq(target_id));
        }else if(type==2){//单聊
            queryBuilder=queryBuilder.where(queryBuilder.or(queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(target_id),Voice_MessageDao.Properties.User_id.eq(current_id)),
                    queryBuilder.and(Voice_MessageDao.Properties.Target_id.eq(current_id),Voice_MessageDao.Properties.User_id.eq(target_id))));
        }
        return queryBuilder.list();
    }
    public void deleteConversation(Conversation conversation){
        List<Voice_Message> vms=null;
        if(conversation.getType().equals("single")){
            vms=getGroupMessage(conversation.getUser_id(),conversation.getTarget_id(),2);
        }else if(conversation.getType().equals("group")){
            vms=getGroupMessage(conversation.getUser_id(),conversation.getTarget_id(),1);
        }
        if(vms!=null){
            for(Voice_Message vm:vms){
                File file=new File(vm.getVoice_path());
                if(file.exists()){
                    file.delete();
                }
                Voice_MessageDao voice_messageDao=daoManager.getDaoSession().getVoice_MessageDao();
                voice_messageDao.deleteByKey(vm.getId());
            }
        }
        ConversationDao conversationDao=daoManager.getDaoSession().getConversationDao();
        conversationDao.deleteByKey(conversation.getId());
    }
    public void MessageReaded(Voice_Message message){//消息状态由未读改已读
        Voice_MessageDao dao=daoManager.getDaoSession().getVoice_MessageDao();
        dao.update(message);
    }
    public List<Voice_Message> getGroupMessage(){
        QueryBuilder<Voice_Message> queryBuilder=daoManager.getDaoSession().queryBuilder(Voice_Message.class);
        queryBuilder.where(Voice_MessageDao.Properties.Type.eq(2)).where(Voice_MessageDao.Properties.Target_id.eq(1)).where(Voice_MessageDao.Properties.User_id.notEq(3));
//        queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.Target_id.eq(target_id)).where(Voice_MessageDao.Properties.Type.eq(type)).offset(page * 10).limit(10).orderDesc(Voice_MessageDao.Properties.Date_time);
//        if(type==2){
//            queryBuilder=queryBuilder.where(Voice_MessageDao.Properties.User_id.eq(current_id));
//        }

        return queryBuilder.list();
    }
    public boolean addConversation(Conversation conversation) {
        boolean flag ;
        flag = daoManager.getDaoSession().insert(conversation) != -1 ? true : false;
        return flag;
    }
    public Conversation getConversation(int target_id,String type,int user_id){
        QueryBuilder<Conversation> queryBuilder=daoManager.getDaoSession().queryBuilder(Conversation.class);
        return queryBuilder.where(ConversationDao.Properties.Target_id.eq(target_id)).where(ConversationDao.Properties.User_id.eq(user_id)).where(ConversationDao.Properties.Type.eq(type)).unique();
    }
    public List<Conversation> getConversationList(int user_id){//获取会话列表
        QueryBuilder<Conversation> queryBuilder=daoManager.getDaoSession().queryBuilder(Conversation.class);
        return queryBuilder.where(ConversationDao.Properties.User_id.eq(user_id)).list();
    }
    public void clearRecord(){
        daoManager.getDaoSession().deleteAll(Voice_Message.class);
        daoManager.getDaoSession().deleteAll(Conversation.class);
    }
}
