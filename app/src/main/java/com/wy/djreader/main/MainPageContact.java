package com.wy.djreader.main;

import android.app.PendingIntent;
import android.os.Bundle;

import com.wy.djreader.base.BasePresenter;
import com.wy.djreader.base.BaseView;

import java.io.File;

/**
 * @INTERFACE MainPageContact
 * @desc 主界面MVP模式的接口联系者
 * @author wy
 * @date 2019/1/21
 */
public interface MainPageContact {
    interface View extends BaseView {
        //显示更新提示
        void showUpdateDialog(Bundle data);
        //显示下载进度条
        void showDownloadBar();
        //更新下载进度
        void updateDownloadProgress(Bundle data);
        //隐藏进度条
        void hideProgressBar();
        //显示Toast
        void showToast(String msg, int showTime);
    }

    interface Presenter extends BasePresenter{
        //检查版本更新
        void checkVersionUpdate();
        //检测当前更新情况
        boolean[] checkUpdateState();
        //存储当前更新的情况
        void saveUpdateState(boolean isUpdating, boolean downloadFinish);
        //获取更新的信息
        Bundle getUpdateInfos();
        //下载apk
        void downLoadApk();
        //安装apk
        PendingIntent installApp(File file, boolean autoInstall);
    }
}
