package com.sj.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

/**
 * @Description: 对话框工具类
 * @author: sj
 * @date: 2022年04月22日 21:51
 */
public class DialogUtil {

    public static void show(Context context,String title,String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.show();
    }

    public static void errot(Context context,String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("错误");
        builder.setMessage(message);

        builder.show();
    }

    public static void error(Context context){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("错误");
        builder.setMessage("未知错误");

        builder.show();
    }

}
