package com.sj.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.sj.R;

public class WalkNavigateActivity extends AppCompatActivity {

    private final static String TAG="WalkNavigateActivity";

    private WalkNavigateHelper mNaviHelper=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_walk_navigate);

        mNaviHelper=WalkNavigateHelper.getInstance();

        View view=mNaviHelper.onCreate(WalkNavigateActivity.this);
        if (view!=null){
            setContentView(view);
        }
        mNaviHelper.startWalkNavi(WalkNavigateActivity.this);
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