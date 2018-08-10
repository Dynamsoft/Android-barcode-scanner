package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.dynamsoft.barcode.afterprocess.jni.AfterProcess;
import com.dynamsoft.barcode.afterprocess.jni.BarcodeRecognitionResult;
import com.dynamsoft.barcode.afterprocess.jni.StitchImageResult;
import com.dynamsoft.barcode.jni.BarcodeReader;
import com.dynamsoft.barcode.jni.BarcodeReaderException;
import com.dynamsoft.barcode.jni.EnumImagePixelFormat;
import com.dynamsoft.barcode.jni.TextResult;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Elemen on 2018/8/9.
 */
public class ResultActivity extends AppCompatActivity {
	@BindView(R.id.scale_imageview)
	SubsamplingScaleImageView scaleImageview;
	@BindView(R.id.pb_progress)
	ProgressBar pbProgress;
	private String path = Environment.getExternalStorageDirectory() + "/dbr-preview-img";
	private BarcodeReader reader;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			scaleImageview.setImage(ImageSource.bitmap((Bitmap) msg.obj));
			pbProgress.setVisibility(View.GONE);
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		ButterKnife.bind(this);
		try {
			reader = new BarcodeReader(getString(R.string.dbr_license));
			JSONObject jsonObject = new JSONObject("{\n" +
					"  \"ImageParameters\": {\n" +
					"    \"Name\": \"Custom_100947_777\",\n" +
					"    \"BarcodeFormatIds\": [\n" +
					"      \"CODE_39\",\n" +
					"      \"CODE_128\",\n" +
					"      \"CODE_93\",\n" +
					"      \"CODABAR\",\n" +
					"      \"ITF\",\n" +
					"      \"EAN_13\",\n" +
					"      \"EAN_8\",\n" +
					"      \"UPC_A\",\n" +
					"      \"UPC_E\"" +
					"    ],\n" +
					"    \"LocalizationAlgorithmPriority\": [\"ConnectedBlock\", \"Lines\", \"Statistics\", \"FullImageAsBarcodeZone\"],\n" +
					"    \"AntiDamageLevel\": 5,\n" +
					"    \"DeblurLevel\":5,\n" +
					"    \"ScaleDownThreshold\": 1000\n" +
					"  },\n" +
					"\"version\":\"1.0\"" +
					"}");
			reader.appendParameterTemplate(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = readImage();
				Message message = handler.obtainMessage();
				message.obj = bitmap;
				handler.sendMessage(message);
			}
		}).start();
	}

	private Bitmap readImage() {
		File file = new File(path);
		String[] fileNames = file.list();
		List<String> arrayList=Arrays.asList(fileNames);
		orderByName(arrayList);
		fileNames= (String[]) arrayList.toArray();
		Bitmap bitmap1;
		Bitmap bitmap2;
		Bitmap bitmap3 = null;
		TextResult[] textResults1;
		TextResult[] textResults2;
		BarcodeRecognitionResult[] localizationResults = null;
		for (int i = 0; i < fileNames.length; i++) {
			Log.d("result act", "result : " + i);
			if (i == 0) {
				bitmap1 = decodeFile(fileNames[0]);
				bitmap2 = decodeFile(fileNames[1]);
				textResults1 = decodeImage(bitmap1);
				textResults2 = decodeImage(bitmap2);
				StitchImageResult result = AfterProcess.stitchImage(convertImage(bitmap1), convertImage(bitmap2),
						EnumImagePixelFormat.IPF_ARGB_8888, bitmap1.getWidth() * 4,
						bitmap2.getWidth() * 4, textResults1, textResults2,
						bitmap1.getWidth(), bitmap1.getHeight(), bitmap2.getWidth(), bitmap2.getHeight());
				switch (result.basedImg) {
					case 0:
						break;
					case 1:
						bitmap3 = bitmap1;
						localizationResults = new BarcodeRecognitionResult[textResults1.length];
						BarcodeRecognitionResult recognitionResult;
						for (int j = 0; j < textResults1.length; j++) {
							recognitionResult = new BarcodeRecognitionResult();
							recognitionResult.barcodeBytes = textResults1[j].barcodeBytes;
							recognitionResult.barcodeText = textResults1[j].barcodeText;
							recognitionResult.pts = textResults1[j].localizationResult.resultPoints;
							localizationResults[j] = recognitionResult;
						}
						break;
					case 2:
						bitmap3 = bitmap2;
						localizationResults = new BarcodeRecognitionResult[textResults2.length];
						BarcodeRecognitionResult recognitionResult1;
						for (int j = 0; j < textResults2.length; j++) {
							recognitionResult1 = new BarcodeRecognitionResult();
							recognitionResult1.barcodeBytes = textResults2[j].barcodeBytes;
							recognitionResult1.barcodeText = textResults2[j].barcodeText;
							recognitionResult1.pts = textResults2[j].localizationResult.resultPoints;
							localizationResults[j] = recognitionResult1;
						}
						break;
					case 3:
						bitmap3 = result.image;
						localizationResults = result.resultArr;
						break;
					default:
						break;
				}
			} else {
				if (fileNames.length <= 2) {
					break;
				}
				if (i + 1 < fileNames.length) {
					bitmap1 = decodeFile(fileNames[i + 1]);
					textResults1 = decodeImage(bitmap1);
					StitchImageResult result = AfterProcess.stitchImage(convertImage(bitmap3), convertImage(bitmap1),
							EnumImagePixelFormat.IPF_ARGB_8888, bitmap3.getWidth() * 4,
							bitmap1.getWidth() * 4, localizationResults, textResults1,
							bitmap3.getWidth(), bitmap3.getHeight(), bitmap1.getWidth(), bitmap1.getHeight());
					switch (result.basedImg) {
						case 0:
							break;
						case 1:
							break;
						case 2:
							bitmap3 = bitmap1;
							localizationResults = new BarcodeRecognitionResult[textResults1.length];
							BarcodeRecognitionResult recognitionResult1;
							for (int j = 0; j < textResults1.length; j++) {
								recognitionResult1 = new BarcodeRecognitionResult();
								recognitionResult1.barcodeBytes = textResults1[j].barcodeBytes;
								recognitionResult1.barcodeText = textResults1[j].barcodeText;
								recognitionResult1.pts = textResults1[j].localizationResult.resultPoints;
								localizationResults[j] = recognitionResult1;
							}
							break;
						case 3:
							bitmap3 = result.image;
							localizationResults = result.resultArr;
							break;
						default:
							break;
					}
				}
			}
		}
		return bitmap3;
	}

	private Bitmap decodeFile(String fileName) {
		File file = new File(path, fileName);
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

	private TextResult[] decodeImage(Bitmap bitmap) {
		try {
			return reader.decodeBufferedImage(bitmap, "");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BarcodeReaderException e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] convertImage(Bitmap bitmap) {
		int bytes = bitmap.getByteCount();
		ByteBuffer buf = ByteBuffer.allocate(bytes);
		bitmap.copyPixelsToBuffer(buf);
		return buf.array();
	}

	public void orderByName(List fliePath) {
		Collections.sort(fliePath, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
	}
}
