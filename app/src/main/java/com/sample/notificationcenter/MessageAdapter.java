package com.sample.notificationcenter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageBean> list;
    private OnItemClickListener itemClickListener;
    public List<Boolean> isClicked;

    //是否是编辑状态
    public boolean editMode;

    public MessageAdapter(Context context, List<MessageBean> list) {
        this.context = context;
        this.list = list;
        isClicked = new ArrayList<>();
        for(int i=0; i<list.size(); i++){
            isClicked.add(false);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        final MessageViewHolder holder = new MessageViewHolder(view);
        //每个item点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener!=null) {
                    isClicked.clear();
                    for(int i=0; i<list.size(); i++){
                        isClicked.add(false);
                    }
                    isClicked.set(holder.getLayoutPosition(),true);
                    notifyDataSetChanged();
                    itemClickListener.onItemClick(holder.getLayoutPosition());
                }
            }
        });

        //复选框选中状态变化的监听
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (itemClickListener != null) {
                    MessageBean bean = list.get(holder.getLayoutPosition());
                    bean.setChecked(b);
                    itemClickListener.onChecked(holder.getLayoutPosition());
                }
            }
        });

        //侧滑删除的点击事件
        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onDelete(holder.getLayoutPosition());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {
        MessageBean bean = list.get(position);
        holder.title.setText(bean.getTitle());
        holder.time.setText(bean.getTime());
        holder.content.setText(bean.getMessage());

        /**
         * 根据是否是编辑模式设定显示是否已读还是显示复选框
         */
        if (editMode) {
            isClicked.clear();
            for(int i=0; i<list.size(); i++){
                isClicked.add(false);
            }
            itemClicked(holder, position);
            holder.read.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);

        } else {
            //非编辑状态需要
            itemClicked(holder, position);
            if (bean.getFlag() == 1) {
                holder.read.setVisibility(View.INVISIBLE);
            } else {
                holder.read.setVisibility(View.VISIBLE);
            }
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        holder.checkBox.setChecked(bean.isChecked());
    }

    //更改item点击时候的效果
    private void itemClicked(@NonNull MessageViewHolder holder, int position) {
        if (isClicked.size() != 0 && isClicked.size() == list.size() && isClicked.get(position)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#999999"));
            holder.content.setTextColor(Color.parseColor("#000000"));
            holder.title.setTextColor(Color.parseColor("#000000"));
            holder.time.setTextColor(Color.parseColor("#000000"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#282828"));
            holder.content.setTextColor(Color.parseColor("#F8F8FF"));
            holder.title.setTextColor(Color.parseColor("#F8F8FF"));
            holder.time.setTextColor(Color.parseColor("#F8F8FF"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onChecked(int position);

        void onDelete(int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ImageView read;
        CheckBox checkBox;
        TextView title;
        TextView time;
        TextView content;
        TextView tv_delete;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            read = itemView.findViewById(R.id.read);
            checkBox = itemView.findViewById(R.id.checkbox);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            content = itemView.findViewById(R.id.content);
            tv_delete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
