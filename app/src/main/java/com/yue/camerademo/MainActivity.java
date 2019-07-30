package com.yue.camerademo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.yue.camerademo.adapter.MainAdapter;
import com.yue.camerademo.bean.MainBean;
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

    private MainAdapter adapter;
    private List<MainBean> list;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
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
        list.add(new MainBean(CameraTest01Activity.class, "相机初级显示", "相机初级显示"));
        adapter.notifyDataSetChanged();
    }
}
