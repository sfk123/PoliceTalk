package com.sheng.android.policetalk.modal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/15.
 */
public class Group implements Parcelable {
    private int id;
    private String name;
    private List<User> mambers;//群组成员
    private List<User> managers;//管理员
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMambers() {
        return mambers;
    }

    public void setMambers(List<User> mambers) {
        this.mambers = mambers;
    }

    public List<User> getManagers() {
        return managers;
    }

    public void setManagers(List<User> managers) {
        this.managers = managers;
    }

    public Group() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeTypedList(this.mambers);
        dest.writeTypedList(this.managers);
    }

    protected Group(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.mambers = in.createTypedArrayList(User.CREATOR);
        this.managers = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}
