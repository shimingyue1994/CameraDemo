package com.yue.camerademo.camera1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.yue.camerademo.R;
import com.yue.camerademo.databinding.ActivityScanningBinding;

/**
 * 相机扫描
 */
public class ScanningActivity extends AppCompatActivity {


    private ActivityScanningBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scanning);
    }
}
