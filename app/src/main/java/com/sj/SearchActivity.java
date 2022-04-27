package com.sj;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

/**
 * @Description: 搜索界面
 * @author: sj
 * @date: 2022年4月22日
 */
public class SearchActivity extends AppCompatActivity {

    private String TAG="search";
    private SuggestionSearch mSuggestionSearch;

    //控件
    private EditText start=null;//检索的起点
    private EditText end=null;//检索的终点
    private Button search=null;//搜索按钮
    private ListView address_list=null;//地址列表
    private EditText currentInput=null;
    private RadioGroup navigate_way=null;//出行方式

    //返回信息
    private LatLng start_pt=null;
    private LatLng end_pt=null;
    private int WayCode=MainActivity.NAVIGATEWAYWALKCODE;//默认出行方式为步行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        widgetIni();

        SuggestionSearchIni();

    }

    /**
     * sug搜索初始化
     */
    private void SuggestionSearchIni() {
        mSuggestionSearch = SuggestionSearch.newInstance();//创建sug搜索示例
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {//创建sug检索监听器，即监听返回结果
            /**
             * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
             * @param suggestionResult sug检索结果
             */
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {

                //处理sug检索结果
                if (suggestionResult==null||suggestionResult.getAllSuggestions()==null){//搜索结果为空直接返回
                    Log.e(TAG,"搜索结果为空");
                    return;
                }

                //搜索结果不为空
                address_list.setAdapter(new AddressAdapter(suggestionResult.getAllSuggestions(),getApplicationContext()));
                address_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        SuggestionResult.SuggestionInfo info = suggestionResult.getAllSuggestions().get(i);
                        currentInput.setText(info.getAddress());

                        if (currentInput.getId()==R.id.start){
                            start_pt=info.getPt();
                        }else if (currentInput.getId()==R.id.end){
                            end_pt=info.getPt();
                        }
                    }
                });

                Log.e(TAG,"搜索结束");
            }
        };
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
    }

    private void widgetIni(){
        start=findViewById(R.id.start);
        start.addTextChangedListener(textWatcher(start));

        end=findViewById(R.id.end);
        end.addTextChangedListener(textWatcher(end));

        search=findViewById(R.id.start_daohang);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNavigate();
            }
        });

        address_list=findViewById(R.id.address_list);

        navigate_way=findViewById(R.id.navigate_way);
        navigate_way.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int WayId = radioGroup.getCheckedRadioButtonId();
                if (WayId==R.id.walk){
                    WayCode=MainActivity.NAVIGATEWAYWALKCODE;
                }else if (WayId==R.id.bike){
                    WayCode=MainActivity.NAVIGATEWAYBIKECODE;
                }else {
                    Log.e(TAG,"未知出行方式");
                }
            }
        });
    }

    /**
     * 编辑框监听事件
     * @param editText
     * @return
     */
    private TextWatcher textWatcher(EditText editText){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentInput=editText;
                String keyword=editText.getText().toString();
                if (keyword.length()>0){//如果输入值长度大于0才进行查询操作
                    //查询
                    if (!mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city("沈阳").keyword(keyword))) {
                        Toast.makeText(getApplicationContext(),"查询失败，请检查网络",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    public void startNavigate(){

//        Log.e(TAG,"/"+start_pt);
//        Log.e(TAG,"/"+end_pt);

        if (start_pt!=null&&end_pt!=null){

            Intent intent=new Intent();

            //起点
            intent.putExtra("start_lat",start_pt.latitude);
            intent.putExtra("start_lng",start_pt.longitude);

            //终点
            intent.putExtra("end_lat",end_pt.latitude);
            intent.putExtra("end_lng",end_pt.longitude);

            //出行方式
            intent.putExtra("way_code",WayCode);

            setResult(Activity.RESULT_OK,intent);//设置返回码
            finish();//关闭活动


        }else {
            AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("警告");
            builder.setMessage("请设置好起点和终点后再开始导航");

            builder.show();
        }
    }

}