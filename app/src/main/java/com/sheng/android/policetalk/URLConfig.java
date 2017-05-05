package com.sheng.android.policetalk;

/**
 * Created by Administrator on 2017/3/14.
 */

public class URLConfig {
//    public static final String host="192.168.88.110:8080/police_talk_server";
    public static final String host="120.25.247.85:8080/police_talk_server";
//    public static final String host="120.86.171.23/police_talk_server";
    private static final String url_login="/app/login";
    private static final String url_get_contacts="/app/getContacts";
    private static final String url_get_voice="/app/getVoice";
    private static final String url_upload_voice="/app/uploadVioce";
    private static final String url_upload_voice_single="/app/uploadVioce_single";
    private static final String url_repwd="/app/repwd";
    private static final String url_upload_photo="/app/uploadPhoto";
    private static final String url_delete_userof_group="/app/deleteGroupMember";
    private static final String url_get_users_add_group="/app/getUsersOfAddToGroup";
    private static final String url_add_users_to_group="/app/addUsersToGroup";
    private static final String url_save_exception="/app/saveException";
    public static String getHostForCookie(){
        return "http://"+host;
    }
    public static String getUrl_login(){
        return "http://"+host+url_login;
    }
    public static String getContactsUrl(){
        return "http://"+host+url_get_contacts;
    }
    public static String getVoice(){return "http://"+host+url_get_voice;}
    public static String getUplaodVoice(){return "http://"+host+url_upload_voice;}
    public static String getUplaodVoiceSingle(){return "http://"+host+url_upload_voice_single;}
    public static String getRePwd(){return "http://"+host+url_repwd;}
    public static String getUploadPhotoUrl(){return "http://"+host+url_upload_photo;}
    public static String getHeadPhoto(String photo){return "http://"+host+"/photo/"+photo;}
    public static String getDeleteGroupUser(){return "http://"+host+url_delete_userof_group;}
    public static String getUserOfAddToGroup(){return "http://"+host+url_get_users_add_group;}
    public static String addUserToGroup(){return "http://"+host+url_add_users_to_group;}
    public static String saveException(){return"http://"+host+url_save_exception; }
}
