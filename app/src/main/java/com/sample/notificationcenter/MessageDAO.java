package com.sample.notificationcenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageDAO {
    //从数据库中删除某条消息
    public static void delete(Context context, MessageBean messageBean){
        Uri uri = Uri.parse(MetaData.TableMetaData.CONTENT_URI.toString() + "/" +messageBean.getId());
        ContentResolver cr = context.getContentResolver();
        cr.delete(uri, null, null);
    }

    //将信息保存到数据库中
    public static void saveMessage(Context context, MessageBean messageBean){
        ContentValues values1 = new ContentValues();
        values1.put("title", messageBean.getTitle());
        values1.put("message", messageBean.getMessage());
        if(messageBean.getFlag() == 1){
            values1.put("flag", 1);
        }else {
            values1.put("flag", 0);
        }
        values1.put("time", messageBean.getTime());
        values1.put("type", messageBean.getType());
        context.getContentResolver().insert(MetaData.TableMetaData.CONTENT_URI, values1);
    }

    /**
     * 从数据库中初始化模拟新闻数据
     */
    public static List<MessageBean> getNews(Context context){
        List<MessageBean> mNewsList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MetaData.TableMetaData.CONTENT_URI, new String[]{"id", MetaData.TableMetaData.FIELD_TITLE, MetaData.TableMetaData.FIELD_MESSAGE,
                MetaData.TableMetaData.FIELD_FLAG, MetaData.TableMetaData.FIELD_TIME, MetaData.TableMetaData.FIELD_TYPE}, null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "当前没有数据", Toast.LENGTH_SHORT).show();
            return null;
        }
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String message = cursor.getString(cursor.getColumnIndex("message"));
            int flag = cursor.getInt(cursor.getColumnIndex("flag"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));
            MessageBean news = new MessageBean(id,title,message,time,flag,type);
            mNewsList.add(news);
        }
        cursor.close();
        return mNewsList;
    }


    //随机生成一个MessageBean对象,设置信息内容
    public static MessageBean getMessage(){
        MessageBean messageBean = new MessageBean();
        int type = (int) (Math.random() * 10 + 1);
        switch (type) {
            case 1:
               messageBean = new MessageBean("行程评分",
                        "本次行驶距离：xx公里；油耗：xx;急加速：xx次；急减速：xx次，急转弯：xx次。建议减速慢行，平稳行驶，可减少油耗，降低安全风险，祝您用车愉快！", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 2);
                break;
            case 2:
                messageBean = new MessageBean("车辆保养提醒",
                        "尊敬的用户，累计行驶公里数，达到保养里程，请联系上汽大通官方4S店预约保养，谢谢。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 3);
                break;
            case 3:
                messageBean = new MessageBean("保养预约到期提醒",
                        "尊敬的用户，您的爱车，在年月日时间有一次维保服务预约。预约门店地址：xxx，联系电话xxx。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 4);
                break;
            case 4:
                messageBean = new MessageBean("车检提醒", "您的【车辆昵称】还有xx天要进行车检，请于x年x月x日前去完成车检，谢谢。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 6);
                break;
            case 5:
                messageBean = new MessageBean("目的地推送", "您收到来自xxx发送的目的地：上海市杨浦区军工路2500号。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 5);
                break;
            case 6:
                messageBean = new MessageBean("行程提醒", "15分钟后开车去公司。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()), 0, 5);
                break;
            case 7:
                messageBean = new MessageBean("低油量提醒", "前油量偏低，点击前往附近加油站加油，保证车辆正常行驶", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 5);
                break;
            case 8:
                messageBean = new MessageBean("可续里程不足", "您的车辆可续里程不足以到达目的地，请前往最近加油站加油。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 5);
                break;
            case 9:
                messageBean = new MessageBean("天气提醒", "明天有雨，请记得带伞。", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 2);
                break;
            case 10:
                messageBean = new MessageBean("促销活动", "运营商提供的活动消息体", new SimpleDateFormat("yyyy-MM-dd  hh:mm").format(new Date()).toString(), 0, 4);
                break;
        }
        return messageBean;
    }
}
