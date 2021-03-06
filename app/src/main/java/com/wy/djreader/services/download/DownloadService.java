package com.wy.djreader.services.download;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wy.djreader.utils.Constant;
import com.wy.djreader.utils.NotificationUtil;
import com.wy.djreader.utils.fileutil.FileOperation;
import com.wy.djreader.utils.httputil.OkHttpImpl;
import com.wy.djreader.utils.httputil.OkHttpUtil;

import java.io.InputStream;

import okhttp3.ResponseBody;

public class DownloadService extends Service {
    private DownloadHandler downloadHandler = null;
    private String downloadUrl;
    private String fileName;
    private boolean isUpdating;
    private PendingIntent pendingIntent;

    private final class DownloadHandler extends Handler{
        public DownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //下载文件(同步下载)
            OkHttpUtil okHttpUtil = new OkHttpImpl();
            Object object = okHttpUtil.syncGet(downloadUrl,OkHttpUtil.ReturnType.FILE,null);
            if (object == null) return;
            ResponseBody responseBody = (ResponseBody) object;
            InputStream inputStream = responseBody.byteStream();
            long contentLength = responseBody.contentLength();
            fileName = fileName +".apk";
            boolean isSuccessful = FileOperation.writeToFile(inputStream,contentLength,Constant.DOWNLOAD_PATH,fileName,true,null);
            //使用广播传递结果
            isUpdating = false;
            //激发activity传递过来的PendingIntent
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //创建一个Handler线程，使用HandlerThread可以在Handler内执行异步任务
        HandlerThread downloadThread = new HandlerThread("downloadThread");
        downloadThread.start();
        //获取Looper，传入Handler
        Looper looper = downloadThread.getLooper();
        downloadHandler = new DownloadHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取传递过来的数据
        downloadUrl = intent.getExtras().getString("apkUrl");
        fileName = intent.getExtras().getString("fileName");
        isUpdating = intent.getExtras().getBoolean("isUpdating");
        pendingIntent = intent.getParcelableExtra("pendingIntent");
        Message msg = downloadHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        downloadHandler.sendMessage(msg);
        return START_REDELIVER_INTENT;//重启服务时传递最后一个intent
    }
}
