package com.yue.camerademo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yue.camerademo.R;
import com.yue.camerademo.databinding.ActivityHtestBinding;

public class HTestActivity extends AppCompatActivity {

    private ActivityHtestBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_htest);
        mBinding.btnVv.setOnClickListener(v -> {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
