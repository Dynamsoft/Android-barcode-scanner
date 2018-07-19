package com.dynamsoft.demo.dynamsoftbarcodereaderdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.R;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.HistoryItemBean;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import cn.bingoogolapple.photopicker.util.BGABrowserPhotoViewAttacher;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import cn.bingoogolapple.photopicker.widget.BGAImageView;

/**
 * Created by Elemen on 2018/7/16.
 */
public class HistoryDetailViewPagerAdapter extends PagerAdapter {
	private ArrayList<HistoryItemBean> picPathList;
	private Context context;

	public HistoryDetailViewPagerAdapter(Context context, ArrayList<HistoryItemBean> picPathList) {
		this.picPathList = picPathList;
		this.context = context;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		BGAImageView imageView = new BGAImageView(context);
		drawRectOnImg(imageView, position);
		container.addView(imageView);
		final BGABrowserPhotoViewAttacher photoViewAttacher = new BGABrowserPhotoViewAttacher(imageView);
		//imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		imageView.setDelegate(new BGAImageView.Delegate() {
			@Override
			public void onDrawableChanged(Drawable drawable) {
				if (drawable != null && drawable.getIntrinsicHeight() > drawable.getIntrinsicWidth()
						&& drawable.getIntrinsicHeight() > BGAPhotoPickerUtil.getScreenHeight()) {
					photoViewAttacher.setIsSetTopCrop(true);
					photoViewAttacher.setUpdateBaseMatrix();
				} else {
					photoViewAttacher.update();
				}
			}
		});
		return imageView;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return picPathList == null ? 0 : picPathList.size();
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}

	private void drawRectOnImg(ImageView imageView, int position) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(9f);
		paint.setColor(context.getResources().getColor(R.color.aboutOK));
		paint.setAntiAlias(true);
		Path path = new Path();
		HistoryItemBean historyItemBean;
		historyItemBean = picPathList.get(position);
		if (historyItemBean != null) {
			Bitmap oriBitmap = BitmapFactory.decodeFile(historyItemBean.getCodeImgPath());
			Bitmap rectBitmap = oriBitmap.copy(Bitmap.Config.ARGB_8888, true);
			Canvas canvas = new Canvas(rectBitmap);
			for (int i = 0; i < historyItemBean.getRectCoord().size(); i++) {
				path.reset();
				path.moveTo(historyItemBean.getRectCoord().get(i)[0].x, historyItemBean.getRectCoord().get(i)[0].y);
				path.lineTo(historyItemBean.getRectCoord().get(i)[1].x, historyItemBean.getRectCoord().get(i)[1].y);
				path.lineTo(historyItemBean.getRectCoord().get(i)[2].x, historyItemBean.getRectCoord().get(i)[2].y);
				path.lineTo(historyItemBean.getRectCoord().get(i)[3].x, historyItemBean.getRectCoord().get(i)[3].y);
				path.close();
				canvas.drawPath(path, paint);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			rectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] bytes = baos.toByteArray();
			Glide.with(context)
					.load(bytes)
					.into(imageView);
		}
	}
}
