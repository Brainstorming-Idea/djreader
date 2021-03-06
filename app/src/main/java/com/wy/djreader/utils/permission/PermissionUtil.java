package com.wy.djreader.utils.permission;

public interface PermissionUtil {

    //检查当前所需权限是否存在
    boolean checkPermission();

    //获取未拥有的权限
    String[] getUnauthorizedPms();
    //请求未拥有的权限
    void requestPermissions(int requestCode);

}
