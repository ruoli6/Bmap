package com.sj.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;

public class BikeNavigateActivity extends AppCompatActivity {

    private final static String TAG = "BikeNavigateActivity";

    private BikeNavigateHelper mNaviHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取BikeNavigateHelper示例
        mNaviHelper = BikeNavigateHelper.getInstance();
        // 获取诱导页面地图展示View
        View view = mNaviHelper.onCreate(BikeNavigateActivity.this);

        if (view != null) {
            setContentView(view);
        }

        // 开始导航
        mNaviHelper.startBikeNavi(BikeNavigateActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
    }
}