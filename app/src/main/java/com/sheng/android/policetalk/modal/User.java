package com.sheng.android.policetalk.modal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 用户表
 * Created by Administrator on 2017/3/15.
 */
public class User implements Parcelable {
    private int id;
    private String phone;
    private String username;
    private String password;
    private String photo;
    private Role role;
    private Boolean is_manger=false;//当获取群组成员时用到此参数
    private Boolean online=false;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIs_manger() {
        return is_manger;
    }

    public void setIs_manger(Boolean is_manger) {
        this.is_manger = is_manger;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.phone);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.photo);
        dest.writeParcelable(this.role, flags);
        dest.writeValue(this.is_manger);
        dest.writeValue(this.online);
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.phone = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.photo = in.readString();
        this.role = in.readParcelable(Role.class.getClassLoader());
        this.is_manger = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
