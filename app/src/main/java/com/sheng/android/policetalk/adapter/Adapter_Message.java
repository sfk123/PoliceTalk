package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.modal.Voice_Message;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.util.MyUtil;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.SimpleFormatter;

/**
 * Created by Administrator on 2017/3/21.
 */

public class Adapter_Message extends RecyclerView.Adapter<Adapter_Message.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private List dataList=null;
    private View.OnClickListener clickListener;
    private User user;
    private Group group;
    private int voice_min_width;//最小语音条宽度
    private int voice_width_step;//每秒增加宽度，满宽按60计算
    private SimpleDateFormat dateFormat;
    public Adapter_Message(Context context, List<?> dataList,User user, View.OnClickListener clickListener,Group group){
        this.context=context;
        this.mInflater=LayoutInflater.from(context);
        this.dataList=dataList;
        this.clickListener=clickListener;
        this.user=user;
        voice_min_width= MyUtil.ToDipSize(40,context);
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        voice_width_step=(wm.getDefaultDisplay().getWidth()-voice_min_width-MyUtil.ToDipSize(140,context))/60;
//        System.out.println("《《《《《《《《《《《《《《《《《《《《《《");
//        System.out.println("每秒宽度："+voice_width_step);
        this.group=group;
        dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Sort();
    }
    public void setMessageList(List<?> dataList,Group group){
        this.dataList=dataList;
        this.group=group;
        Sort();
        notifyDataSetChanged();
    }
    private void Sort(){
        Collections.sort(dataList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if(o1 instanceof Voice_Message&&o2 instanceof Voice_Message){
                    Voice_Message g1=(Voice_Message)o1;
                    Voice_Message g2=(Voice_Message)o2;
                    if(g1.getDate_time().getTime()>g2.getDate_time().getTime()){
                        return 1;
                    }else{
                        return -1;
                    }
                }else{

                }
                return 0;
            }
        });
    }
    private String getNameById(int user_id){
        if(group==null){
            return "";
        }
        for(User user:group.getMambers()){
            if(user.getId()==user_id){
                return user.getUsername();
            }
        }
        return "已删除的用户";
    }
    private User getUser(int user_id){
        if(group==null){
            return null;
        }
        for(User user:group.getMambers()){
            if(user.getId()==user_id){
                return user;
            }
        }
        return null;
    }
    /**
     * item显示类型
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public Adapter_Message.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view=mInflater.inflate(R.layout.item_message_list,parent,false);
        Adapter_Message.ViewHolder viewHolder=new Adapter_Message.ViewHolder(view);
        viewHolder.layout_content_body.setOnClickListener(clickListener);
        return viewHolder;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name,tv_second,tv_time;
        public RoundImageView img_head_left,img_head_right;
        public RelativeLayout layout_content_body;
        public ImageView img_sound;
        public View point;
        public ViewHolder(View view){
            super(view);
            tv_name = (TextView)view.findViewById(R.id.tv_name);
            tv_second = (TextView)view.findViewById(R.id.tv_second);
            img_head_left=(RoundImageView)view.findViewById(R.id.img_head_left);
            img_head_right=(RoundImageView)view.findViewById(R.id.img_head_right);
            layout_content_body=(RelativeLayout)view.findViewById(R.id.layout_content_body);
            img_sound=(ImageView)view.findViewById(R.id.img_sound);
            tv_time=(TextView)view.findViewById(R.id.tv_time);
            point=view.findViewById(R.id.point);
        }
    }
    /**
     * 数据的绑定显示
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(Adapter_Message.ViewHolder holder, int position) {
        Voice_Message item=(Voice_Message)dataList.get(position);
        holder.tv_time.setText(dateFormat.format(item.getDate_time()));
        if(user.getId()==item.getUser_id()){//当前用户的语音
            holder.img_head_right.setVisibility(View.VISIBLE);
            Picasso.with(context).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_current).into(holder.img_head_right);
//            holder.img_head_right.setImageResource(R.mipmap.head_current);
            holder.img_head_left.setVisibility(View.INVISIBLE);
            holder.tv_name.setVisibility(View.GONE);
            int minute=item.getVoice_length()/1000/60;
            int second=item.getVoice_length()/1000;
            String second_str;
            if(minute==0){
                second_str=second+"''";
            }else{
                second=second-minute*60;
                second_str=minute+"'"+second+"''";
            }
            //DateFormat.format("yyyy-MM-dd HH:mm:ss",((Group_Message) item).getDate_time().getTime())+
            holder.tv_second.setText(second_str);
//                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//                System.out.println(second_str);
            RelativeLayout.LayoutParams rlp=(RelativeLayout.LayoutParams)holder.layout_content_body.getLayoutParams();
            rlp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            second=item.getVoice_length()/1000;
            rlp.width=voice_min_width+second*voice_width_step;
            holder.layout_content_body.setLayoutParams(rlp);
            rlp=(RelativeLayout.LayoutParams)holder.tv_second.getLayoutParams();
            rlp.removeRule(RelativeLayout.RIGHT_OF);
            rlp.addRule(RelativeLayout.LEFT_OF,R.id.layout_content_body);
            holder.tv_second.setLayoutParams(rlp);
            holder.layout_content_body.setBackgroundResource(R.drawable.chart_bg_right);
            rlp=(RelativeLayout.LayoutParams)holder.img_sound.getLayoutParams();
            rlp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.img_sound.setImageResource(R.mipmap.ico_sound_right);
            holder.layout_content_body.setTag(item);
            holder.point.setVisibility(View.GONE);
        }else{//别人发来的语音
            holder. img_head_left.setVisibility(View.VISIBLE);
            User user1=getUser(item.getUser_id());
            if(user1!=null)
            Picasso.with(context).load(URLConfig.getHeadPhoto(user1.getPhoto())).placeholder(R.mipmap.head_online).into(holder.img_head_left);
            else//用户已踢出该组
                holder.img_head_left.setImageResource(R.mipmap.head_online);
            holder.img_head_right.setVisibility(View.INVISIBLE);
            int minute=item.getVoice_length()/1000/60;
            int second=item.getVoice_length()/1000;
            String second_str;
            if(minute==0){
                second_str=second+"''";
            }else{
                second=second-minute*60;
                second_str=minute+"'"+second+"''";
            }
            holder.tv_second.setText(second_str);
            RelativeLayout.LayoutParams rlp=(RelativeLayout.LayoutParams)holder.tv_second.getLayoutParams();
            rlp.removeRule(RelativeLayout.LEFT_OF);
            rlp.addRule(RelativeLayout.RIGHT_OF,R.id.layout_content_body);
            holder.tv_second.setLayoutParams(rlp);
            holder.layout_content_body.setBackgroundResource(R.drawable.chart_bg_left);
            rlp=(RelativeLayout.LayoutParams)holder.layout_content_body.getLayoutParams();
            rlp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            second=item.getVoice_length()/1000;
            rlp.width=voice_min_width+second*voice_width_step;
            holder.layout_content_body.setLayoutParams(rlp);
            rlp=(RelativeLayout.LayoutParams)holder.img_sound.getLayoutParams();
            rlp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT );
            holder.img_sound.setImageResource(R.mipmap.ico_sound);
            holder.layout_content_body.setTag(item);
            if(item.getRead()){
                holder.point.setVisibility(View.GONE);
            }else{
                holder.point.setVisibility(View.VISIBLE);
            }
        }
        if(item.getType()==1){
            if(user.getId()==item.getUser_id()){
                holder.tv_name.setVisibility(View.GONE);
            }else{
                holder.tv_name.setVisibility(View.VISIBLE);
                holder.tv_name.setText(getNameById( item.getUser_id()));
            }
            holder.tv_time.setTextColor(Color.WHITE);
        }else if(item.getType()==2){//单聊消息
            holder.tv_name.setVisibility(View.GONE);
        }else{
            holder.tv_time.setTextColor(Color.WHITE);
            holder.tv_name.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    //添加单条数据
    public void addItem(Object object) {
        //mTitles.add(position, data);
        //notifyItemInserted(position);
        dataList.add(object);
        notifyDataSetChanged();
    }
    public Voice_Message getFirst(){
        if(dataList.size()==0)
            return null;
        return (Voice_Message)dataList.get(0);
    }
    //添加数据
    public void addItems(List newDatas) {
        //mTitles.add(position, data);
        //notifyItemInserted(position);
        dataList.addAll(0,newDatas);
        Sort();
        notifyDataSetChanged();
    }
}
