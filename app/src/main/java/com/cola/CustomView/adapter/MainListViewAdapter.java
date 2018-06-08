package com.cola.CustomView.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.netease.wangkai1.myapplication.R;

import java.util.ArrayList;

public class MainListViewAdapter extends BaseAdapter {

    private ArrayList<String> mDataList;

    public MainListViewAdapter(){
        mDataList = mockData();
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0:mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return View.inflate(parent.getContext(), R.layout.item_view, null);
    }

    private ArrayList<String> mockData(){
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            list.add(i+" number");
        }
        return list;
    }
}
