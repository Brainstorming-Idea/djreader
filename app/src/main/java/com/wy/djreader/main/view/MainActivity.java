package com.wy.djreader.main.view;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wy.djreader.R;
import com.wy.djreader.base.BaseActivity;
import com.wy.djreader.base.BasePresenter;
import com.wy.djreader.databinding.ActivityMainBinding;
import com.wy.djreader.document.view.DocFragment;
import com.wy.djreader.function.view.Function_fragment;
import com.wy.djreader.main.MainPageContact;
import com.wy.djreader.main.presenter.MainPagePresenter;
import com.wy.djreader.personal.view.MeFragment;
import com.wy.djreader.utils.ActivityUtil;
import com.wy.djreader.utils.DialogUtil;
import com.wy.djreader.utils.ToastUtil;

public class MainActivity extends BaseActivity implements MainPageContact.View, DocFragment.docFragmentInteractionListener,Function_fragment.appFragmentInteractionListener,MeFragment.meFragmentInteractionListener {

    private Toolbar mToolbar;
    private DocFragment docFragment;
    private MeFragment meFragment;
    private Function_fragment function_fragment;
    private FragmentManager fragmentManager;
    private MainPageContact.Presenter mainPresenter;
    private ActivityMainBinding mainBinding = null;
    private Context context;

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.fileMg:
                    ActivityUtil.replaceFragment(fragmentManager,docFragment,R.id.fragment_container);
                    break;
                case R.id.functionMg:
                    ActivityUtil.replaceFragment(fragmentManager,function_fragment,R.id.fragment_container);
                    break;
                case R.id.accountMg:
                    ActivityUtil.replaceFragment(fragmentManager,meFragment,R.id.fragment_container);
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
        mainPresenter = new MainPagePresenter(this,context);
        return mainPresenter;
    }

    @Override
    protected void initDataBinding(ViewDataBinding dataBinding) {
        mainBinding = (ActivityMainBinding) dataBinding;
    }

    @Override
    protected void initialize() {
        //检查APP更新
        mainPresenter.checkVersionUpdate();
        //添加fragment
        docFragment = DocFragment.newInstance("","");
        function_fragment = Function_fragment.newInstance("","");
        meFragment = MeFragment.newInstance("","");
        fragmentManager = getSupportFragmentManager();
        ActivityUtil.addFragmentToActivity(fragmentManager,docFragment,R.id.fragment_container);
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
    public void meFragmentInteraction(Uri uri) {

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
                data.getString("description"),context.getString(R.string.update_Positive),
                context.getString(R.string.update_Negative));
        // 当点确定按钮时从服务器上下载 新的apk 然后安装
        DialogInterface.OnClickListener positiveListener = (dialog,which)->{
            //dialog方式下载
            mainPresenter.downLoadApk();
            //通知栏下载
//                Intent intentSer = new Intent(context,UpdateService.class);
//                Bundle xmlData = new Bundle();
//                xmlData.putString("apkUrl", info.getUrl());
//                intentSer.putExtras(xmlData);
//                context.startService(intentSer);
            dialog.dismiss();
        };
        DialogInterface.OnClickListener negativeListener = (dialog,which)->{
            dialog.dismiss();
        };
        DialogUtil.showDialog(context,dialogInfo,false,null,positiveListener,negativeListener);
    }

    @Override
    public void showToast(String msg, int showTime) {
        ToastUtil.toastMessage(context,msg,showTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}