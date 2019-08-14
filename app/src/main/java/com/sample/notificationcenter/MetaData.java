package com.sample.notificationcenter;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 常量类
 */
public class MetaData {
    //数据库名
    public static final String DATABASE_NAME="messagecenter.db";
    //版本号
    public static final int DATABASE_VERSION = 1;
    //授权名称-和manifest里的一样
    //程序通过设置这个Uri才能对自定义里面的数据表进行操作
    public static final String AUTHORITY= "com.sample.notificationcenter.newsprovider";
    //表名
    public static final String TABLE_NAME = "messages";

    /**内部类，封装关于表的字段信息,实现BaseColumns接口
     * 开发人员不必在定义字段_ID和_Count了(因为实现了BaseColumns)
     * static声明--只属于类而不是属于对象
     * */
    public static final class TableMetaData implements BaseColumns {

        //Uri，外部程序需要访问就是通过这个Uri访问的，这个Uri必须的唯一的。
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/messages");
        // 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.sample.notificationcenter.newsprovider.messages";
        // 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
        public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/vnd.com.sample.notificationcenter.newsprovider.messages";

        //表的属性-表明  属性名
        //public static final String TABLE_NAME = "news";
        public static final String FIELD_TITLE = "title";
        public static final String FIELD_MESSAGE = "message";
        public static final String FIELD_FLAG = "flag";
        public static final String FIELD_TIME = "time";
        public static final String FIELD_TYPE = "type";

        /* 自定义匹配码 */
        public static final int MESSAGE = 1;
        /* 自定义匹配码 */
        public static final int MESSAGES = 2;

        //URI,目前只有这么一种类型
        //注意，里面是表名

        //多记录,这是用来URI得到Type(类型为删除数据型时)，仿照系统的返回值
        /***此方法返回一个所给Uri的指定数据的MIME类型。它的返回值如果以vnem开头，
         * 那么就代表这个Uri指定的是单条数据。如果是以vnd.Android.cursor.dir开头的话
         * ，那么说明这个Uri指定的是全部数据。/
         */

        //DESC(降序)，ASC(升序),这句话的意思为按ID升序
        //public static final String DEFAULT_SORT_ORDER=_ID+" ASC";

        public static final UriMatcher uriMatcher;
        static {
            // 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            // 如果match()方法匹配content://com.example.messagecenter.newsprovider/news,返回匹配码为NEWS
            uriMatcher.addURI(MetaData.AUTHORITY, "messages", MESSAGES);
            // 如果match()方法匹配content://com.example.messagecenter.newsprovider/news/1,路径，返回匹配码为NEW
            uriMatcher.addURI(MetaData.AUTHORITY, "messages/#", MESSAGE);
        }
    }
}
