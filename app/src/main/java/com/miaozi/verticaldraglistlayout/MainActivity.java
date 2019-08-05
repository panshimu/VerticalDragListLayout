package com.miaozi.verticaldraglistlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private List<String> mItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.lv);
        mItems = new ArrayList<>();
        for (int i=0;i<200;i++){
            mItems.add(" i -- > " + i);
        }
        mListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public String getItem(int i) {
                return mItems.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                TextView tv = (TextView) LayoutInflater.from(MainActivity.this).inflate(R.layout.item,viewGroup,false);
                tv.setText(getItem(i));
                return tv;
            }
        });
    }
}
