package com.blogofyb.forum.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.SubscribePlateAdapter;
import com.blogofyb.forum.beans.PlateBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubscribePlateActivity extends BaseActivity {
    private final int GET_PLATES_SUCCESS = 0;
    private final int GET_PLATES_FAILED = 1;
    private final int REFRESH_SUCCESS = 2;

    private List<PlateBean> mPlates;
    private SubscribePlateAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Button mSubscribe;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_PLATES_SUCCESS:
                    showData();
                    break;
                case GET_PLATES_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(SubscribePlateActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(false);
                    refreshData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_subscribe_plate);
        mRecyclerView = findViewById(R.id.rv_Subscribe_plate);
        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_plates);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBeans();
            }
        });
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitle("订阅板块");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(SubscribePlateActivity.this);
            }
        });

        loadBeans();
    }

    private void loadBeans() {
        mPlates = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.GET_PLATES_WITHOUT_ACCOUNT, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        PlateBean plateBean = new PlateBean();
                        plateBean.setPlateName(object.getString(Keys.PLATE_NAME));
                        plateBean.setIcon(object.getString(Keys.ICON));
                        plateBean.setId(object.getString(Keys.ID));
                        mPlates.add(plateBean);
                    }
                    Message message = new Message();
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        message.what = REFRESH_SUCCESS;
                    } else {
                        message.what = GET_PLATES_SUCCESS;
                    }
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = GET_PLATES_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new SubscribePlateAdapter(mPlates);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void refreshData() {
        mAdapter.refreshData(mPlates);
    }
}
