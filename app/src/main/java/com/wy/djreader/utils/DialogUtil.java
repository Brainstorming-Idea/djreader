package com.wy.djreader.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

public class DialogUtil {
    public static void showDialog(Context context, Bundle data, boolean cancelable, View view, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener){
        //初始化Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (view != null){
            builder.setView(view);
        }
        builder.setTitle(data.getString("title"))
                .setMessage(data.getString("message"))
                .setCancelable(cancelable);
        builder.setPositiveButton(data.getString("positive"), positiveListener);
        //取消事件
        builder.setNegativeButton(data.getString("negative"), negativeListener);
        //显示
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static Bundle getDialogData(String title, String message, String positive, String negative){
        Bundle data = new Bundle();
        data.putString("title",title);
        data.putString("message",message);
        data.putString("positive",positive);
        data.putString("negative",negative);
        return data;
    }

    public static void showProgressBar(ProgressBar progressBar,Bundle data,int style, boolean indeterminate, int progress, int max){
        progressBar.setIndeterminate(indeterminate);
        progressBar.setProgress(progress);
        progressBar.setMax(max);
    }

}
