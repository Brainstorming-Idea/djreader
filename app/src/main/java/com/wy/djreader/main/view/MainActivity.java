package com.wy.djreader.main.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.wy.djreader.R;
import com.wy.djreader.base.BaseActivity;
import com.wy.djreader.base.BasePresenter;
import com.wy.djreader.databinding.ActivityMainBinding;
import com.wy.djreader.document.view.DocFragment;
import com.wy.djreader.function.view.Function_fragment;
import com.wy.djreader.main.MainPageContact;
import com.wy.djreader.main.presenter.MainPagePresenter;
import com.wy.djreader.main.viewmodel.MainViewModel;
import com.wy.djreader.personal.view.MeFragment;
import com.wy.djreader.services.download.DownloadService;
import com.wy.djreader.utils.ActivityUtil;
import com.wy.djreader.utils.Constant;
import com.wy.djreader.utils.DialogUtil;
import com.wy.djreader.utils.NotificationUtil;
import com.wy.djreader.utils.ToastUtil;
import com.wy.djreader.utils.permission.PermissionUtil;
import com.wy.djreader.utils.permission.PermissionUtilImpl;

import java.io.File;

public class MainActivity extends BaseActivity implements MainPageContact.View, DocFragment.docFragmentInteractionListener, Function_fragment.appFragmentInteractionListener{

    private Toolbar mToolbar;
    private DocFragment docFragment;
    private MeFragment meFragment;
    private Function_fragment function_fragment;
    private FragmentManager fragmentManager;
    private MainPageContact.Presenter mainPresenter;
    private ActivityMainBinding mainBinding = null;
    private Context context;
    private boolean isUpdating;
    private boolean downloadFinish;
    private BroadcastReceiver mainReceiver;
    private MainViewModel mainViewModel = new MainViewModel();

    /**
     * 广播接收
     */
    class MainReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            isUpdating = intent.getExtras().getBoolean("isUpdating");
            downloadFinish = intent.getExtras().getBoolean("downloadFinish");
            Log.i("isUpdating",isUpdating+"");
            mainPresenter.saveUpdateState(isUpdating,downloadFinish);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.fileMg:
                    ActivityUtil.replaceFragment(fragmentManager, docFragment, R.id.fragment_container);
                    break;
                case R.id.functionMg:
                    ActivityUtil.replaceFragment(fragmentManager, function_fragment, R.id.fragment_container);
                    break;
                case R.id.accountMg:
                    ActivityUtil.replaceFragment(fragmentManager, meFragment, R.id.fragment_container);
                    break;
            }
            return true;//将选中项目显示为选中
        }
    };

    @Override
    protected int getLayoutId() {
        context = this;
        return R.layout.activity_main;
    }

    @Override
    protected BasePresenter initPresenter() {
        mainPresenter = new MainPagePresenter(this, context);
        return mainPresenter;
    }

    @Override
    protected void initDataBinding(ViewDataBinding dataBinding) {
        mainBinding = (ActivityMainBinding) dataBinding;
    }

    @Override
    protected void initialize() {
        //注册广播接收者
        mainReceiver = new MainReceiver();
        IntentFilter mainFilter = new IntentFilter("com.wy.djreader.main.view.MainActivity");
        registerReceiver(mainReceiver,mainFilter);

        //检查APP更新
        mainPresenter.checkVersionUpdate();
        //添加fragment
        docFragment = DocFragment.newInstance("", "");
        function_fragment = Function_fragment.newInstance("", "");
        meFragment = MeFragment.newInstance("","");
        fragmentManager = getSupportFragmentManager();
        ActivityUtil.addFragmentToActivity(fragmentManager, docFragment, R.id.fragment_container);
        //BottomNavigationView点击事件
        mainBinding.bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    /**
     *
     * @param uri
     */
    @Override
    public void docFragmentInteraction(Uri uri) {

    }

    @Override
    public void appFragmentInteraction(Uri uri) {

    }

    /**
     * @desc 为Fragment设置Presenter
     * @author wy
     * @date 2018/12/24 17:59
     * @params
     * @return
     */
    @Override
    public void setPresenter(Object presenter) {

    }

    @Override
    public void showUpdateDialog(Bundle data) {
        Bundle dialogInfo = DialogUtil.getDialogData(context.getString(R.string.update_title),
                data.getString("description"), context.getString(R.string.update_Positive),
                context.getString(R.string.update_Negative));
        // 当点确定按钮时从服务器上下载 新的apk 然后安装
        DialogInterface.OnClickListener positiveListener = (dialog, which) -> {
            //首先检查一下是否有存储权限
            String[] permissions = Constant.PermissionConstant.READ_WRITE_EXTERNAL_STORAGE;
            PermissionUtil permissionUtil = new PermissionUtilImpl(permissions, context);
            if (!permissionUtil.checkPermission()) {
                permissionUtil.requestPermissions(Constant.PermissionConstant.REQUEST_CODE_1);
            } else {
//                //dialog方式下载
//                mainPresenter.downLoadApk();
                /*通知栏下载*/
                downloadWithNotification();
            }
            dialog.dismiss();
        };
        //lambda表达式
        DialogInterface.OnClickListener negativeListener = (dialog, which) -> dialog.dismiss();
        DialogUtil.showDialog(context, dialogInfo, false, null, positiveListener, negativeListener);
    }

    /**
     * 通知栏显示下载
     */
    private void downloadWithNotification() {
        File apkFile = new File(Constant.DOWNLOAD_PATH+mainPresenter.getUpdateInfos().getString("versionName")+".apk");
        //显示通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            NotificationUtil.initNotification(context,null, apkFile, NotificationManager.IMPORTANCE_LOW);
        }else {
            NotificationUtil.initNotification(context,null, apkFile, NotificationCompat.PRIORITY_LOW);
        }

        NotificationUtil.updateNotification(Constant.Notification.NOTIFY_ID_1,100,0,false,"");
        //正在下载
        isUpdating = true;
        //启动服务
        Intent intentSer = new Intent(this,DownloadService.class);
        Bundle xmlData = new Bundle();
        String downloadUrl = mainPresenter.getUpdateInfos().getString("appUpdateUrl");
        String fileName = mainPresenter.getUpdateInfos().getString("versionName");
        xmlData.putString("apkUrl", downloadUrl);
        xmlData.putString("fileName",fileName);
        xmlData.putBoolean("isUpdating",isUpdating);
        intentSer.putExtras(xmlData);
        //获取一个与Broadcast关联的PendingIntent(在需要传递当前activity中的参数时使用，此处多余)
        Intent broIntent = new Intent();
        broIntent.setAction("com.wy.djreader.main.view.MainActivity");
        broIntent.putExtra("isUpdating",false);
        broIntent.putExtra("downloadFinish",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),Constant.PendingIntent.REQUESTCODE_1,broIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        //将PendingIntent传给服务
        intentSer.putExtra("pendingIntent",pendingIntent);
        //启动服务
        startService(intentSer);
    }

    @Override
    public void showDownloadBar() {
        ProgressBar progressBar = mainBinding.downloadBar;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateDownloadProgress(Bundle data) {
        int progress = data.getInt("progress");
        int total = data.getInt("total");
        mainViewModel.setProgress(progress);
        mainBinding.setMainViewModel(mainViewModel);
    }

    @Override
    public void hideProgressBar() {
        mainBinding.downloadBar.setVisibility(View.GONE);
        ToastUtil.toastMessage(context,"下载成功",ToastUtil.SHORT);
    }

    /**
     * 权限申请回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.PermissionConstant.REQUEST_CODE_1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //dialog方式下载
//                    mainPresenter.downLoadApk();
                    /*通知栏下载*/
                    downloadWithNotification();
                } else {
                    ToastUtil.toastMessage(context,"存储权限被拒绝，无法进行更新",ToastUtil.LONG);
                }
                break;
        }
    }

    @Override
    public void showToast(String msg, int showTime) {
        ToastUtil.toastMessage(context,msg,showTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播
        unregisterReceiver(mainReceiver);
    }
}
