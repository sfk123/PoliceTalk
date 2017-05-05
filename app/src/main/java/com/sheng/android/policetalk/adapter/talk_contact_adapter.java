package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2017/3/20.
 */

public class talk_contact_adapter extends BaseAdapter {
    private List<User> users;
    private LayoutInflater layoutInflater;
    private ImgGrayTransformation grayColorFilter;
    private Context mContext;
    public talk_contact_adapter(List<User> users,Context context){
        this.layoutInflater = LayoutInflater.from(context);
        this.users=users;
        grayColorFilter=new ImgGrayTransformation();
        mContext=context;
    }
    /**
     *获取列数
     */
    public int getCount() {
        return users.size();
    }
    /**
     *获取某一位置的数据
     */
    public Object getItem(int position) {
        return users.get(position);
    }
    /**
     *获取唯一标识
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * android绘制每一列的时候，都会调用这个方法
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            holder = new ViewHolder();
            // 获取组件布局
            convertView = layoutInflater.inflate(R.layout.item_talk_contacts, null);
            holder.setTv((TextView)convertView.findViewById(R.id.tv_item));
            holder.setImg_head((RoundImageView)convertView.findViewById(R.id.img_head));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        User user=users.get(position);
        holder.getTv().setText(user.getUsername());
        if(user.getOnline()){
            holder.getTv().setTextColor(Color.BLACK);
            Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(holder.getImg_head());
//            holder.getImg_head().setImageResource(R.mipmap.head_online);
        }else{
            holder.getTv().setTextColor(Color.DKGRAY);
            if(user.getPhoto()==null||user.getPhoto().equals(""))
                holder.getImg_head().setImageResource(R.mipmap.head_offline);
            else
                Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_offline).transform(grayColorFilter).into(holder.getImg_head());
//            holder.getImg_head().setImageResource(R.mipmap.head_offline);
        }
        return convertView;
    }
    class ViewHolder{
        private TextView tv;
        private RoundImageView img_head;
        public TextView getTv() {
            return tv;
        }

        public void setTv(TextView tv) {
            this.tv = tv;
        }

        public RoundImageView getImg_head() {
            return img_head;
        }

        public void setImg_head(RoundImageView img_head) {
            this.img_head = img_head;
        }
    }
}
