package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/15.
 */

public class Adapter_SelectUser extends RecyclerView.Adapter<Adapter_SelectUser.ViewHolder> implements View.OnClickListener {
    private Context context;
    private LayoutInflater mInflater;
    private List<User> users;
    private ImgGrayTransformation grayColorFilter;
    private List<String> selectIds;
    public Adapter_SelectUser(Context context,List<User> users){
        this.mInflater=LayoutInflater.from(context);
        this.users=users;
        this.context=context;
        selectIds=new ArrayList<>();
        grayColorFilter = new ImgGrayTransformation();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view=mInflater.inflate(R.layout.item_contacts_select,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onClick(View v) {
        User user=(User)v.getTag();
        String id=String.valueOf(user.getId());
        if(selectIds.contains(id)){
            selectIds.remove(id);
        }else{
            selectIds.add(id);
        }
        notifyDataSetChanged();
    }
    public List<String> getSelectIds(){
        return selectIds;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_tv;
        public RoundImageView head;
        public ImageView img_selected;
        public ViewHolder(View view){
            super(view);
            item_tv = (TextView)view.findViewById(R.id.tv_item);
            head=(RoundImageView)view.findViewById(R.id.img_head);
            img_selected=(ImageView)view.findViewById(R.id.img_selected);
        }
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user=users.get(position);
        holder.item_tv.setText(user.getUsername());
        if(user.getOnline()){
            Picasso.with(context).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(holder.head);
        }else{
            if(user.getPhoto()==null||user.getPhoto().equals(""))
                holder.head.setImageResource(R.mipmap.head_offline);
            else
                Picasso.with(context).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_offline).transform(grayColorFilter).into(holder.head);
        }
        if(selectIds.contains(String.valueOf(user.getId()))){
            holder.img_selected.setImageResource(R.mipmap.selct_on);
        }else{
            holder.img_selected.setImageResource(R.mipmap.selct);
        }
        holder.itemView.setTag(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
