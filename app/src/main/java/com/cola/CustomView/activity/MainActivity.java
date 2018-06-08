package com.cola.CustomView.activity;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cola.CustomView.adapter.MainListViewAdapter;
import com.cola.CustomView.adapter.MainRecyclerViewAdapter;
import com.cola.CustomView.view.pulltorefresh.PullToRefreshRecyclerViewBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.netease.wangkai1.myapplication.R;

public class MainActivity extends AppCompatActivity {
    private PullToRefreshListView mListView;

    //private PullToRefreshRecyclerViewBase mRefreshView;
    //private RecyclerView mRecyclerView;

    /**
     * view
     */
    private boolean hasFooter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list_view);
        initUI();
    }

    private Handler myHandler = new Handler();
    private void initUI(){
        mListView = findViewById(R.id.list_view);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                //解决立刻调用onRefreshComplete，无法将头布局隐藏的问题
                //PullToRefreshListView调用onRefreshComplete方法 无法取消刷新的bug
//                myHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mListView.onRefreshComplete();
//                    }
//                }, 500);
            }
        });
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            //下拉刷新监听
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

                hasFooter  = false;
                Toast.makeText(getApplicationContext(), "onPullDownToRefresh", Toast.LENGTH_SHORT).show();
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshComplete();
                    }
                }, 500);
            }

            //上拉加载监听
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!hasFooter){
                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }else{

                }
                Toast.makeText(getApplicationContext(), "onPullDownToRefresh", Toast.LENGTH_SHORT).show();
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshComplete();
                    }
                }, 500);
            }
        });
        ListView listView = mListView.getRefreshableView();
        TextView textView1 = new TextView(getApplicationContext());
        textView1.setText("toutoutoutout");
        listView.addHeaderView(textView1);

        TextView textView2 = new TextView(getApplicationContext());
        textView2.setText("toutoutoutout");
        listView.addHeaderView(textView2);

        mListView.setAdapter(new MainListViewAdapter());
    }


}
