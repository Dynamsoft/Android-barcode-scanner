package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.adapter.HistoryListAdapter;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.DBRImage;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.HistoryItemBean;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.DBRCache;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.baseadapter.BGADivider;
import cn.bingoogolapple.baseadapter.BGAOnItemChildClickListener;

/**
 * Created by Elemen on 2018/7/3.
 */
public class HistoryActivity extends BaseActivity implements BGAOnItemChildClickListener {
	@BindView(R.id.rlv_history)
	RecyclerView rlvHistory;
	private DBRCache mCache;
	private HistoryListAdapter historyListAdapter;
	private ArrayList<HistoryItemBean> listItem;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_history;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		ButterKnife.bind(this);
		mCache = DBRCache.get(this);
		setToolbarBackgroud("#ffffff");
		setToolbarTitle("History");
		setToolbarTitleColor("#000000");
		setToolbarNavIcon(R.drawable.ic_action_back);
		historyListAdapter = new HistoryListAdapter(rlvHistory);
		historyListAdapter.setOnItemChildClickListener(this);
		fillHistoryList();
	}

	private void fillHistoryList() {
		List<DBRImage> imageList = LitePal.findAll(DBRImage.class);
		Collections.reverse(imageList);
		if (imageList.size() > 16) {
			imageList = imageList.subList(0, 15);
		}
		historyListAdapter.setData(imageList);
		rlvHistory.addItemDecoration(BGADivider.newShapeDivider());
		rlvHistory.setLayoutManager(getLinearLayoutManager());
		rlvHistory.setAdapter(historyListAdapter);

/*		File file = new File(getCacheDir() + "/DBRCache");
		fileNames = file.list();
		HistoryItemBean historyItemBean;
		listItem = new ArrayList<>();
		if (fileNames != null && fileNames.length > 0) {
			for (int i = 0; i < fileNames.length; i++) {
				try {
					historyItemBean = LoganSquare.parse(mCache.getAsStringWithFileName(fileNames[i]),
							HistoryItemBean.class);
					if (historyItemBean != null) {
						listItem.add(historyItemBean);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			historyListAdapter.setData(listItem);
			rlvHistory.addItemDecoration(BGADivider.newShapeDivider());
			rlvHistory.setLayoutManager(getLinearLayoutManager());
			rlvHistory.setAdapter(historyListAdapter);
		}*/
	}

	private RecyclerView.LayoutManager getLinearLayoutManager() {
		return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
	}

	@Override
	public void onItemChildClick(ViewGroup parent, View childView, int position) {
		switch (childView.getId()) {
			case R.id.cl_item_history:
				Intent intent = new Intent(this, HistoryItemDetailActivity.class);
				intent.putExtra("page_type", 1);
				intent.putExtra("position", position);
				startActivity(intent);
				break;
			default:
				break;
		}
	}
}
