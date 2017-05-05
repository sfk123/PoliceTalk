package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.fragment.FragmentMessage;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2017/3/20.
 */

public class RefreshRecyclerAdapter extends RecyclerView.Adapter<RefreshRecyclerAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Conversation> conversationList=null;
    private FragmentMessage fragmentMessage;
    private View.OnClickListener clickListener;
    private Context mContext;
    private ImgGrayTransformation grayColorFilter;
    public RefreshRecyclerAdapter(Context context, List<Conversation> conversationList,FragmentMessage fragmentMessage,View.OnClickListener clickListener){
        this.mInflater=LayoutInflater.from(context);
        this.conversationList=conversationList;
        this.fragmentMessage=fragmentMessage;
        this.clickListener=clickListener;
        mContext=context;
        grayColorFilter = new ImgGrayTransformation();
    }
    /**
     * item显示类型
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view=mInflater.inflate(R.layout.item_conversation,parent,false);
        //这边可以做一些属性设置，甚至事件监听绑定
        //view.setBackgroundColor(Color.RED);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(clickListener);
        return viewHolder;
    }

    /**
     * 数据的绑定显示
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Conversation conversation=conversationList.get(position);
        if(conversation.getUnread_count()!=0){
            holder.bar_num.setText(String.valueOf(conversation.getUnread_count()));
            holder.bar_num.setVisibility(View.VISIBLE);
        }else{
            holder.bar_num.setVisibility(View.GONE);
        }
        if(conversation.getType().equals("single")){
            User user=fragmentMessage.getUser(conversation.getTarget_id());
            System.out.println(user.getUsername()+"----"+user.getId());
            if(user!=null) {
                holder.item_tv.setText(user.getUsername());
                if (user.getOnline()) {
                    Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(holder.head);
//                holder.head.setImageResource(R.mipmap.head_online);
                } else {
                    if (user.getPhoto() == null || user.getPhoto().equals(""))
                        holder.head.setImageResource(R.mipmap.head_offline);
                    else
                        Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_offline).transform(grayColorFilter).into(holder.head);
                }
            }else{
                holder.item_tv.setText("已删除的用户");
                holder.head.setImageResource(R.mipmap.head_offline);
            }
        }else if(conversation.getType().equals("group")){
            Group group=fragmentMessage.getGroup(conversation.getTarget_id());
            if(group!=null)
                holder.item_tv.setText(group.getName());
            else
                holder.item_tv.setText("已删除的群组");
            holder.head.setImageResource(R.mipmap.head_group);
        }
        holder.itemView.setTag(conversation);
    }
    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_tv,bar_num;
        public RoundImageView head;
        public ViewHolder(View view){
            super(view);
            item_tv = (TextView)view.findViewById(R.id.tv_item);
            head=(RoundImageView)view.findViewById(R.id.img_head);
            bar_num=(TextView)view.findViewById(R.id.bar_num);
        }
    }

    //添加数据
    public void addItems(List<Conversation> newDatas) {
        //mTitles.add(position, data);
        //notifyItemInserted(position);
        conversationList.addAll(newDatas);
        notifyDataSetChanged();
    }
}
