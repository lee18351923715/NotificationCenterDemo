package com.sample.notificationcenter;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        MessageAdapter.OnItemClickListener {

    private SlideRecyclerView recyclerView;

    private List<MessageBean> list;//数据源
    private MessageAdapter adapter;

    private int position;

    private View visibilityLayout;
    private TextView newsTitleText;
    private TextView newsContentText;
    private Button leftButton;
    private Button rightButton;

    private TextView noMessage;
    private TextView tvUnread;
    private Button all;
    private Button read;
    private Button delete;
    private Button edit;
    private Button send;

    //点击了全选
    private boolean checkAll;
    //编辑模式
    private boolean editMode = false;
    //对应全选按钮
    private boolean clicked;
    //区分是不是删除确认界面
    private boolean confirm;

    private List<Integer> selectedPosition = new ArrayList<>();//选中的位置集合

    private int unread;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        updateUnRead();
        //广播监听 收到广播后会模拟接收到服务端推送，自动添加数据
        ADBReceiver.boardcastListensr = new ADBReceiver.BoardcastListener() {
            @Override
            public void insertMessage() {
                setData();
            }
        };
    }

    private void initView() {
        send = findViewById(R.id.send);
        noMessage = findViewById(R.id.no_message);
        visibilityLayout = findViewById(R.id.visibility_layout);
        newsTitleText = findViewById(R.id.news_title);
        newsContentText = findViewById(R.id.news_content);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);

        tvUnread = findViewById(R.id.unread);
        all = findViewById(R.id.selectall_button);
        read = findViewById(R.id.readed_button);
        delete = findViewById(R.id.delete_main_button);
        edit = findViewById(R.id.edit_button);

        send.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        all.setOnClickListener(this);
        read.setOnClickListener(this);
        delete.setOnClickListener(this);
        edit.setOnClickListener(this);

        recyclerView = findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //增加或减少条目动画效果，不要就注掉
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //数据源初始化，从数据库中获取数据
        if (MessageDAO.getNews(this) == null) {
            list = new ArrayList<>();
        } else {
            list = MessageDAO.getNews(this);
        }
        adapter = new MessageAdapter(this, list);
        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);
        //根据有无数据确定是否显示无消息页面
        if (list.size() == 0) {
            edit.setVisibility(View.INVISIBLE);
            setNoMessage();
        } else {
            edit.setVisibility(View.VISIBLE);
            setMessage();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.edit_button://编辑按钮
                editMessage();
                recyclerView.closeMenu();
                break;
            case R.id.selectall_button://全选按钮
                selectAll();
                break;
            case R.id.readed_button://已读按钮
                messageRead();
                break;
            case R.id.delete_main_button://删除按钮
                deleteMessage();
                break;
            case R.id.left_button:
            case R.id.right_button://消息内容展示页面中两个button的功能
                buttonConfirm(id);
                break;
            case R.id.send://广播发送按钮
                Intent intent = new Intent("adb.addmessage");
                intent.setComponent(new ComponentName("com.sample.notificationcenter", "com.sample.notificationcenter.ADBReceiver"));
                sendBroadcast(intent);
                break;
        }
    }

    /**
     * 编辑按钮
     */
    private void editMessage() {
        editMode = !editMode;
        if (editMode) {
            //编辑状态
            showEdit(View.VISIBLE);
            edit.setText("取消");
            visibilityLayout.setVisibility(View.INVISIBLE);
        } else {
            selectedPosition.clear();
            //取消编辑状态
            showEdit(View.INVISIBLE);
            edit.setText("编辑");
            all.setText("全选");
            clicked = false;
            checkAll = false;
            for (MessageBean bean : list) {
                bean.setChecked(false);
            }
            visibilityLayout.setVisibility(View.INVISIBLE);
        }
        adapter.editMode = editMode;//将编辑状态传递给adapter
        adapter.notifyDataSetChanged();
    }

    /**
     * 全选按钮
     */
    private void selectAll() {
        clicked = !clicked;
        visibilityLayout.setVisibility(View.INVISIBLE);
        if (clicked) {
            //全选
            all.setText("取消全选");
            checkAll = true;
            //全选过程中如果存在未读消息则已读按钮Enable为true，如果不存在那么已读按钮Enable为false
            int index = 0;
            for (int i = 0; i < list.size(); i++) {
                MessageBean bean = list.get(i);
                bean.setChecked(true);
                selectedPosition.add(i);
                if (bean.getFlag() == 0) {
                    index++;
                }
            }
            if (index != 0) {
                if (!read.isEnabled()) {
                    read.setEnabled(true);
                    delete.setEnabled(true);
                }
            } else {
                if (!read.isEnabled()) {
                    read.setEnabled(false);
                    delete.setEnabled(true);
                }
            }
            //取消全选,清空记录复选框被选中的bean对象的位置的集合，并将所有bean对象的checked属性设为false
        } else {
            selectedPosition.clear();
            read.setEnabled(false);
            delete.setEnabled(false);
            //反选
            all.setText("全选");
            checkAll = false;

            for (MessageBean bean : list) {
                bean.setChecked(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 设为已读按钮
     */
    private void messageRead() {
        int index = 0;//记录完成已读消息的数量
        //设置选中item的已读状态
        for (int i = 0; i < selectedPosition.size(); i++) {
            MessageBean bean = list.get(selectedPosition.get(i));
            if (bean.getFlag() == 0) {
                index++;
            }
            bean.setFlag(1);
            MessageDAO.update(this, bean);//bean对象已读状态发生变化后同步更新到数据库中
        }
        selectedPosition.clear();
        unEdit();//回到未编辑状态
        updateUnRead();
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除按钮点击事件
     */
    private void deleteMessage() {
        if(selectedPosition.size()!=list.size()){
            //删除选中的所有信息，首先确认删除消息
            delete();//删除操作，将所选信息删除掉
            updateUnRead();
            unEdit();//回到未编辑状态页面
            adapter.notifyDataSetChanged();
        }else {
            confirm = true;//进入删除模式(右面的页面会提示是否确认删除)
            visibilityLayout.setVisibility(View.VISIBLE);
            newsTitleText.setVisibility(View.INVISIBLE);
            newsContentText.setText("确定删除所选（有）信息吗");
            rightButton.setVisibility(View.VISIBLE);
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("确定删除");
            rightButton.setText("取消");
        }
    }

    /**
     * 主页面回到未编辑状态
     */
    public void unEdit(){
        if(list.size() == 0){
            edit.setVisibility(View.INVISIBLE);
        }
        showEdit(View.INVISIBLE);
        visibilityLayout.setVisibility(View.INVISIBLE);
        edit.setText("编辑");
        editMode = !editMode;
        adapter.editMode = editMode;
        read.setEnabled(false);
        delete.setEnabled(false);
        all.setText("全选");
        checkAll = false;
        for (MessageBean bean : list) {
            bean.setChecked(false);
        }
    }

    /**
     * 删除操作
     */
    private void delete() {
        //删除选中的所有信息，首先确认删除消息
        List<MessageBean> removeList = new ArrayList<>();
        for (int i = 0; i < selectedPosition.size(); i++) {
            int position = selectedPosition.get(i);
            MessageBean bean = list.get(position);
            removeList.add(bean);
            MessageDAO.delete(this, bean);
        }
        //删除选中item对应的数据
        list.removeAll(removeList);
        //清空已选中item的position数据
        selectedPosition.clear();
    }

    /**
     * 右边界面的左右两个按钮的点击事件处理
     *
     * @param id R.id.left_button为左边按钮，R.id.right_button为右边按钮
     */
    private void buttonConfirm(int id) {
        if (!confirm) {
            //不是删除页面，只需要显示信息内容
            int type = list.get(position).getType();
            showToast(id, type);
        } else {
            //需要显示删除页面
            confirm = false;
            if (id == R.id.left_button) {
                delete();
                updateUnRead();
            } else if (id == R.id.right_button) {
                //取消按钮功能
                selectedPosition.clear();
                unEdit();
                recyclerView.closeMenu();
                adapter.notifyDataSetChanged();
                return;
            }
            //删除后回到最初的页面，如果没数据了，则设置编辑键不可选中
            unEdit();
            if (list.size() == 0) {
                edit.setVisibility(View.INVISIBLE);
                setNoMessage();
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 列表item点击事件处理,adapter中接口方法的实现
     *
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        checkAll = false;
        all.setText("全选");
        clicked = false;
        this.position = position;
        MessageBean bean = list.get(position);
        if (!editMode) {
            //非编辑模式点击item
            recyclerView.closeMenu();
            refresh(bean.getTitle(), bean.getMessage(), bean.getType());
            bean.setFlag(1);
            MessageDAO.update(this, bean);//将已读状态同步到数据库中
        } else {
            //编辑模式下点击item
            boolean isChecked = bean.isChecked();
            isChecked = !isChecked;
            bean.setChecked(isChecked);
            adapter.notifyItemChanged(position, bean.isChecked());
        }
        updateUnRead();
    }

    //设置无消息时的页面布局
    public void setNoMessage() {
        noMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    //设置有消息时的页面布局
    public void setMessage() {
        noMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    //更改未读消息数量
    private void updateUnRead() {
        unread = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getFlag() == 0) {
                unread++;
            }
        }
        if (unread == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setText(String.valueOf(unread));
        }
    }

    /**
     * 列表中checkbox选中状态变化的处理
     *
     * @param position
     */
    @Override
    public void onChecked(int position) {
        this.position = position;
        MessageBean bean = list.get(position);
        setCheckData(bean.isChecked());
    }

    @Override
    public void onDelete(final int position) {
        this.position = position;
        MessageDAO.delete(this, list.get(position));
        list.remove(position);
        visibilityLayout.setVisibility(View.INVISIBLE);
        updateUnRead();
        if (list.size() == 0) {
            edit.setVisibility(View.INVISIBLE);
            setNoMessage();
            read.setEnabled(false);
            delete.setEnabled(false);
            all.setText("全选");
            checkAll = false;
            adapter.notifyDataSetChanged();
        }
        adapter.notifyItemRemoved(position);
    }

    /**
     * 设置列表中的选中项
     *
     * @param checked
     */
    private void setCheckData(boolean checked) {
        if (checkAll) {
            return;
        }

        all.setText("全选");
        clicked = false;

        if (checked) {
            //checkbox为选中状态
            selectedPosition.add(position);
            int index = 0;
            for (int i = 0; i < selectedPosition.size(); i++) {
                if (list.get(selectedPosition.get(i)).getFlag() == 0) {
                    index++;
                }
            }
            /**
             * 若选中未读消息的数量不为0，那么已读按钮可以点击，否则的话已读按钮不可以点击
             */
            if (index != 0) {
                if (!read.isEnabled()) {
                    read.setEnabled(true);
                    delete.setEnabled(true);
                }
            } else {
                if (!read.isEnabled()) {
                    read.setEnabled(false);
                    delete.setEnabled(true);
                }
            }

        } else {
            //checkbox为没有被选中状态,需要从selectposition中移除该位置
            int size = selectedPosition.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    if (selectedPosition.get(i) == position) {
                        selectedPosition.remove(i);
                        break;
                    }
                }
            }
            //移除该位置后若checkbox选中的集合大小为0，已读和删除按钮不可点击
            if (selectedPosition.size() == 0) {
                read.setEnabled(false);
                delete.setEnabled(false);
            }
        }

        if (selectedPosition.size() == list.size()) {
            all.setText("取消全选");
            clicked = true;
        }else {
            all.setText("全选");
            clicked = false;
        }
    }
    /**
     * 点击编辑后显示各个功能按钮,
     *
     * @param visibility View.VISIBLE表示显示全选等功能按钮，View.INVISIBLE表示不显示全选等功能按钮
     */
    private void showEdit(int visibility) {
        all.setVisibility(visibility);
        read.setVisibility(visibility);
        delete.setVisibility(visibility);
        read.setEnabled(false);
        delete.setEnabled(false);
    }

    /**
     * 右边按钮点击后对应的Toast
     *
     * @param id
     * @param type
     */
    private void showToast(int id, int type) {
        String text = "";
        if (id == R.id.left_button) {
            switch (type) {
                case 5:
                    text = "导航成功";
                    break;
                case 6:
                    text = "成功车检";
                    break;
                case 3:
                    text = "保养成功";
                    break;
                case 4:
                    text = "查看详情";
                    break;
            }
        } else if (id == R.id.right_button) {
            if (type == 6) {
                text = "查看详情";
            }
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据不同type来设置右边界面的显示
     *
     * @param newsTitle
     * @param newsContent
     * @param type
     */
    private void refresh(String newsTitle, String newsContent, int type) {
        visibilityLayout.setVisibility(View.VISIBLE);
        newsTitleText.setText(newsTitle);//刷新新闻标题
        newsTitleText.setVisibility(View.VISIBLE);
        newsContentText.setText(newsContent);//刷新新闻内容
        switch (type) {
            case 2:
                rightButton.setVisibility(View.INVISIBLE);
                leftButton.setVisibility(View.INVISIBLE);
                break;
            case 5:
                leftButton.setText("导航");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                leftButton.setText("我已车检");
                rightButton.setText("查看详情");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.VISIBLE);
                break;
            case 3:
                leftButton.setText("前去保养");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
            case 4:
                leftButton.setText("查看详情");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * 手动添加数据
     */
    private void setData() {
        MessageBean bean = MessageDAO.getMessage();
        MessageDAO.saveMessage(this, bean);
        List<MessageBean> temp = MessageDAO.getNews(this);
        Collections.reverse(temp);
        bean = temp.get(temp.size() - 1);
        list.add(0, bean);
        if (adapter.isClicked.size() != 0) {
            adapter.isClicked.set(0, false);
        }
        if (adapter != null) {
            if (list.size() == 1) {
                edit.setVisibility(View.VISIBLE);
                setMessage();
            }
            //删除滑动回去，刷新列表数据
            recyclerView.closeMenu();
            adapter.notifyItemInserted(0);
            /**
             * 如果不需要动画效果
             * 就删掉 adapter.notifyItemInserted(lastPosition);
             * 用 adapter.notifyDataSetChanged();
             */
            recyclerView.scrollToPosition(0);
            updateUnRead();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
