package com.yue.camerademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.yue.camerademo.adapter.MainAdapter;
import com.yue.camerademo.bean.MainBean;
import com.yue.camerademo.camera1.CameraSimpleActivity;
import com.yue.camerademo.camera1.CameraTest01Activity;
import com.yue.camerademo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shimy
 * @create 2019/7/30 11:28
 * @desc 摄像头测试camera1 camera2 采集方向等
 */
public class MainActivity extends AppCompatActivity {
    private final static int REQ_PERMISSION_CODE = 0x1000;
    private MainAdapter adapter;
    private List<MainBean> list;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkPermission();
        init();
        initData();
    }

    private void init() {
        list = new ArrayList<>();
        adapter = new MainAdapter(this, list);
        mBinding.recyclerMain.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerMain.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position) -> {
            startActivity(new Intent(MainActivity.this, list.get(position).getActivity()));
        });
    }

    private void initData() {
        list.add(new MainBean(CameraSimpleActivity.class, "相机简单预览", "相机简单预览"));
        list.add(new MainBean(CameraTest01Activity.class, "相机初级显示", "相机初级显示"));
        adapter.notifyDataSetChanged();
    }
    //////////////////////////////////    动态权限申请   ////////////////////////////////////////

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();

            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        (String[]) permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED != ret) {
                        Toast.makeText(this, "用户没有允许需要的权限，使用可能会受到限制！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }
}
