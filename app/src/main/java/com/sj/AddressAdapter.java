package com.sj;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.SuggestionResult;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends BaseAdapter {
    private List<SuggestionResult.SuggestionInfo> infos;
    private Context context;

    public AddressAdapter(List<SuggestionResult.SuggestionInfo> infos, Context context) {
        this.infos=new ArrayList<>();
//        this.infos = infos;
        for (SuggestionResult.SuggestionInfo info : infos) {
            if (info.getPt()!=null&&info.getAddress()!=null){
                this.infos.add(info);
            }
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int i) {
        return infos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.address_item, viewGroup, false);
        }

        SuggestionResult.SuggestionInfo suggestionInfo = infos.get(i);
        LatLng pt = suggestionInfo.getPt();

        if (pt!=null){
//            Log.e("ad", "/" + suggestionInfo.getAddress());
//            Log.e("ad", "/" + suggestionInfo.getPt().toString());

            //初始化控件
            TextView address_name = view.findViewById(R.id.address_name);//地名
            TextView address_lng=view.findViewById(R.id.address_lng);//经度
            TextView address_lat=view.findViewById(R.id.address_lat);//纬度

            address_name.setText(suggestionInfo.getAddress());
            address_lng.setText("经度："+pt.longitude);
            address_lat.setText("纬度："+pt.latitude);
        }


        return view;
    }
}
