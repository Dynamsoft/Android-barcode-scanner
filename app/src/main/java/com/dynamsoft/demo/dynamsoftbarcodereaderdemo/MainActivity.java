package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.dynamsoft.barcode.PublicRuntimeSettings;
import com.dynamsoft.barcode.afterprocess.jni.AfterProcess;
import com.dynamsoft.barcode.afterprocess.jni.CoordsMapResult;
import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.BarcodeReaderException;
import com.dynamsoft.barcode.EnumImagePixelFormat;
import com.dynamsoft.barcode.LocalizationResult;
import com.dynamsoft.barcode.PublicParameterSettings;
import com.dynamsoft.barcode.Point;
import com.dynamsoft.barcode.TextResult;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.DBRImage;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.DBRSetting;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.HistoryItemBean;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.RectCoordinate;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.RectPoint;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.YuvInfo;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.DBRCache;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.DBRUtil;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.FrameUtil;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.weight.HUDCanvasView;
import com.orhanobut.logger.Logger;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.Resolution;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.camera.CameraParameters;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
	private final int DETECT_BARCODE = 0x0001;
	private final int OBTAIN_PREVIEW_SIZE = 0x0002;
	//private final int BARCODE_RECT_COORD = 0x0003;
	private final int BARCODE_RESULT = 0x0004;
	private final int REQUEST_CHOOSE_PHOTO = 0x0001;
	private final int REQUEST_SETTING = 0x0002;
	private final int RESPONSE_GENERAL_SETTING = 0x0001;
	private final int RESPONSE_MULTIBEST_SETTING = 0X0002;
	private final int RESPONSE_MULTIBAL_SETTING = 0X0003;
	private final int RESPONSE_PANORMA_SETTING = 0x0004;
	private boolean isDestroy = false;
	private boolean beepSoundEnabled;
	@BindView(R.id.cameraView)
	CameraView cameraView;
	@BindView(R.id.tv_flash)
	Button mFlash;
	@BindView(R.id.scanCountText)
	TextView mScanCount;
	@BindView(R.id.hud_view)
	HUDCanvasView hudView;
	@BindView(R.id.drag_text)
	TextView dragText;
	@BindView(R.id.sliding_drawer)
	SlidingDrawer slidingDrawer;
	@BindView(R.id.rl_barcode_list)
	ListView lvBarcodeList;
	@BindView(R.id.btn_capture)
	Button btnCapture;
	@BindView(R.id.btn_start)
	Button btnStart;
	@BindView(R.id.btn_finish)
	Button btnFinish;
	@BindView(R.id.tv_drag_qty)
	TextView tvQTY;
	@BindView(R.id.iv_pull)
	ImageView ivPull;
	@BindView(R.id.drag_view)
	TableRow dragView;
	@BindView(R.id.btn_done)
	Button btnDone;
	@BindView(R.id.line_done)
	RelativeLayout lineDone;
	//@BindView(R.id.tv_first_text)
	//TextView tvFirstText;
	//@BindView(R.id.tv_first_format)
	//TextView tvFirstFormat;
	private BarcodeReader reader;
	private TextResult[] result;
	String templateType;
	private boolean isDetected = true;
	private boolean isCameraStarted = false;
	private boolean isDrawerExpand = false;
	private boolean isSingleMode = false;
	private boolean detectStart = false;
	private DBRCache mCache;
	private DBRCache mSettingCache;
	private DBRSetting mSetting;
	private boolean isFlashOn = false;
	private ArrayList<String> allResultText = new ArrayList<>();
	private float previewScale;
	private Resolution previewSize = null;
	private FrameUtil frameUtil;
	private List<Map<String, String>> recentCodeList = new ArrayList<>();
	private SimpleAdapter simpleAdapter;
	private long startDetectTime = 0;
	private long endDetectTime = 0;
	private int frameTime = 0;
	private String path = Environment.getExternalStorageDirectory() + "/dbr-preview-img";
	private ExecutorService threadManager = Executors.newSingleThreadExecutor();
	private boolean hasCameraPermission;
	private Fotoapparat fotoapparat;
	private YuvInfo yuvInfo;
	private YuvInfo readySaveYuvInfo;
	private ArrayList<YuvInfo> yuvInfoList = new ArrayList<>();
	private long duringTime;
	private Menu mMenu;
	private int hgt;
	private int wid;
	private int flag = 0;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case DETECT_BARCODE:
					if (!isDrawerExpand) {
						TextResult[] result = (TextResult[]) msg.obj;
						if (result != null) {
							fulFillRecentList(result);
							for (TextResult aResult : result) {
								if (!allResultText.contains(aResult.barcodeText)) {
									allResultText.add(aResult.barcodeText);
								}
							}
							int count = allResultText.size();
							if (count > 1) {
								mScanCount.setText(count + " Barcodes Scanned");
							} else {
								mScanCount.setText(count + " Barcode Scanned");
							}
							tvQTY.setText("QTY: " + result.length);
							drawDocumentBox(frameUtil.handlePoints(result, previewScale, hgt, wid));
							//tvFirstFormat.setText(DBRUtil.getCodeFormat(result[0].barcodeFormat + ""));
							//tvFirstText.setText(result[0].barcodeText);
						} else {
							tvQTY.setText("QTY: 0");
							hudView.clear();
						}
					}
					break;
				/*case BARCODE_RECT_COORD:
					drawDocumentBox((ArrayList<RectPoint[]>) msg.obj);
					break;*/
				case OBTAIN_PREVIEW_SIZE:
					obtainPreviewScale();
					break;
				case BARCODE_RESULT:
					if (! isDestroy) {
						showResults((TextResult[]) msg.obj);
					}
				default:
					break;
			}
		}
	};


	@Override
	protected int getLayoutId() {
		return R.layout.activity_main;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		ButterKnife.bind(this);
		askForPermissions();
		setToolbarBackgroud("#000000");
		setToolbarNavIcon(R.drawable.ic_action_back_dark);
		setToolbarTitleColor("#ffffff");
		initTemplate();
		initUI();
		frameUtil = new FrameUtil();
		mCache = DBRCache.get(this, 1000 * 1000 * 50, 16);
		setupFotoapparat();
		/*File file = new File(Environment.getExternalStorageDirectory(),"1534411536760");
		byte[] buffer = null;
		try {
			FileInputStream fs = new FileInputStream(file);
			buffer = new byte[fs.available()];
			fs.read(buffer);
			fs.close();
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		try {
			reader.decodeBuffer(buffer, 1920, 1080, 1920, EnumImagePixelFormat.IPF_NV21, "Custom");
		}catch (BarcodeReaderException ex){
			ex.printStackTrace();
		}*/
	}

	private void initTemplate() {
		try {
			reader = new BarcodeReader(getString(R.string.dbr_license));
			mSettingCache = DBRCache.get(this, "SettingCache");
			templateType = mSettingCache.getAsString("templateType");
			String beepSound = mSettingCache.getAsString("beepSound");
			if (beepSound != null) {
				beepSoundEnabled = Boolean.parseBoolean(beepSound);
			} else {
				mSettingCache.put("beepSound", "true");
			}
			if ("GeneralSetting".equals(templateType)) {
				String setting = mSettingCache.getAsString("GeneralSetting");
				if (setting != null) {
					reader.initRuntimeSettingsWithString(setting, 2);
				} else {
					DBRSetting generalSetting = new DBRSetting();
					DBRSetting.ImageParameter generalImgP = new DBRSetting.ImageParameter();
					generalSetting.setImageParameter(generalImgP);
					mSettingCache.put("GeneralSetting", LoganSquare.serialize(generalSetting));
					reader.initRuntimeSettingsWithString(LoganSquare.serialize(generalSetting), 2);
				}
				lineDone.setVisibility(View.VISIBLE);
				btnStart.setVisibility(View.GONE);
				btnFinish.setVisibility(View.GONE);
				slidingDrawer.setVisibility(View.GONE);
				detectStart = true;
				mScanCount.setVisibility(View.GONE);
				setToolbarTitle("General Scan");
			} else if ("MultiBestSetting".equals(templateType)) {
				DBRSetting multiBest = new DBRSetting();
				DBRSetting.ImageParameter multiBestImgP = new DBRSetting.ImageParameter();
				multiBestImgP.setAntiDamageLevel(7);
				multiBestImgP.setDeblurLevel(9);
				multiBestImgP.setScaleDownThreshold(1000);
				multiBest.setImageParameter(multiBestImgP);
				mSettingCache.put("MultiBestSetting", LoganSquare.serialize(multiBest));
				reader.initRuntimeSettingsWithString(LoganSquare.serialize(multiBest), 2);
				lineDone.setVisibility(View.VISIBLE);
				btnStart.setVisibility(View.GONE);
				btnFinish.setVisibility(View.GONE);
				slidingDrawer.setVisibility(View.GONE);
				mScanCount.setVisibility(View.GONE);
				detectStart = true;
				setToolbarTitle("Best Coverage");
			} else if ("OverlapSetting".equals(templateType)) {
				DBRSetting multiBal = new DBRSetting();
				DBRSetting.ImageParameter multiBalImgP = new DBRSetting.ImageParameter();
				multiBalImgP.setAntiDamageLevel(5);
				multiBalImgP.setDeblurLevel(5);
				multiBalImgP.setScaleDownThreshold(1000);
				multiBalImgP.setLocalizationAlgorithmPriority(new ArrayList<String>() {{
					add("ConnectedBlock");
					add("Lines");
					add("Statistics");
					add("FullImageAsBarcodeZone");
				}});
				multiBal.setImageParameter(multiBalImgP);
				lineDone.setVisibility(View.GONE);
				btnFinish.setVisibility(View.GONE);
				slidingDrawer.setVisibility(View.VISIBLE);
				mSettingCache.put("OverlapSetting", LoganSquare.serialize(multiBal));
				reader.initRuntimeSettingsWithString(LoganSquare.serialize(multiBal), 2);
				btnStart.setVisibility(View.GONE);
				detectStart = true;
				setToolbarTitle("Overlap");
			} else if ("PanoramaSetting".equals(templateType)){
				DBRSetting panorma = new DBRSetting();
				DBRSetting.ImageParameter panormaImgP = new DBRSetting.ImageParameter();
				panormaImgP.setAntiDamageLevel(7);
				panormaImgP.setDeblurLevel(9);
				panormaImgP.setScaleDownThreshold(1000);
				panorma.setImageParameter(panormaImgP);
				mSettingCache.put("PanoramaSetting", LoganSquare.serialize(panorma));
				reader.initRuntimeSettingsWithString(LoganSquare.serialize(panorma), 2);
				setToolbarTitle("Panorma");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestroy = true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if ("PanoramaSetting".equals(templateType)) {
			menu.findItem(R.id.menu_share).setVisible(false);
			menu.findItem(R.id.menu_capture).setVisible(false);
			menu.findItem(R.id.menu_file).setVisible(false);
			menu.findItem(R.id.menu_scanning).setVisible(false);
			menu.findItem(R.id.menu_Setting).setVisible(true);
		}
		if ("OverlapSetting".equals(templateType)) {
			menu.findItem(R.id.menu_file).setVisible(false);
			menu.findItem(R.id.menu_capture).setVisible(false);
			menu.findItem(R.id.menu_scanning).setVisible(false);
		}
		mMenu = menu;
		return super.onPrepareOptionsMenu(menu);
	}
	private void setupSlidingDrawer() {
		slidingDrawer.addSlideListener(new SlidingDrawer.OnSlideListener() {
			@Override
			public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
				if (slidingDrawer.getState() == SlidingDrawer.COLLAPSED) {
					//Logger.d("sliding drawer 0");
					isDrawerExpand = false;
					recentCodeList.clear();
					dragText.setText("Pull up to see more results");
					ivPull.setImageResource(R.drawable.arrow_up);
				} else if (slidingDrawer.getState() == SlidingDrawer.EXPANDED) {
					//Logger.d("sliding drawer 1");
					isDrawerExpand = true;
					ivPull.setImageResource(R.drawable.arrow_down);
					dragText.setText("Pull down to continue");
					hudView.clear();

				}
			}
		});
	}

	private void askForPermissions() {
		String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (!EasyPermissions.hasPermissions(this, perms)) {
			hasCameraPermission = false;
			//EasyPermissions.requestPermissions(this, "We need camera permission to provide service.", 0, perms);
			ActivityCompat.requestPermissions(this, perms, 1);
		} else {
			hasCameraPermission = true;
			cameraView.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MenuItem scan = mMenu.findItem(R.id.menu_scanning);
		MenuItem capture = mMenu.findItem(R.id.menu_capture);
		switch (item.getItemId()) {
			case R.id.menu_scanning:
				item.setIcon(R.mipmap.video_able);
				capture.setIcon(R.mipmap.capture_unable);
				switchToMulti();
				break;
			case R.id.menu_capture:
				item.setIcon(R.mipmap.capture_able);
				scan.setIcon(R.mipmap.video_unable);
				switchToSingle();
				break;
			case R.id.menu_file:
				choicePhotoWrapper();
				break;
			case R.id.menu_Setting:
				goToSetting();
				break;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CHOOSE_PHOTO && resultCode == RESULT_OK) {
			String filePath = BGAPhotoHelper.getFilePathFromUri(data.getData());
			Intent intent = new Intent(MainActivity.this, HistoryItemDetailActivity.class);
			intent.putExtra("page_type", 2);
			intent.putExtra("FilePath", filePath);
			startActivity(intent);
		}
		if (requestCode == REQUEST_SETTING) {
			String setting = "";
			mSettingCache = DBRCache.get(this, "SettingCache");
			beepSoundEnabled = Boolean.parseBoolean(mSettingCache.getAsString("beepSound"));
			if (resultCode == RESPONSE_GENERAL_SETTING) {
				setting = mSettingCache.getAsString("GeneralSetting");
			}
			if (resultCode == RESPONSE_MULTIBEST_SETTING) {
				setting = mSettingCache.getAsString("MultiBestSetting");
			}
			if (resultCode == RESPONSE_MULTIBAL_SETTING) {
				setting = mSettingCache.getAsString("OverlapSetting");
			}
			if (resultCode == RESPONSE_PANORMA_SETTING) {
				setting = mSettingCache.getAsString("PanoramaSetting");
			}
			try {
				reader = new BarcodeReader(getString(R.string.dbr_license));
				reader.initRuntimeSettingsWithString(setting, 2);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (hasCameraPermission) {
			fotoapparat.start();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (hasCameraPermission) {
			fotoapparat.stop();
		}
	}

	@OnClick(R.id.tv_flash)
	public void onFlashClick() {
		if (isFlashOn) {
			isFlashOn = false;
			fotoapparat.updateConfiguration(
					UpdateConfiguration.builder()
							.flash(off())
							.build()
			);
		} else {
			isFlashOn = true;
			fotoapparat.updateConfiguration(
					UpdateConfiguration.builder()
							.flash(torch())
							.build()
			);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		hasCameraPermission = true;
		fotoapparat.start();
		cameraView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

	}
	private void showResults(TextResult[] results){
		View dialogView = LayoutInflater.from(this).inflate(R.layout.result_dialog, null);
		ListView resultListView = (ListView)dialogView.findViewById(R.id.lv_result_list);
		TextView resultTime = (TextView)dialogView.findViewById(R.id.tv_result_decode_time);
		TextView resultCount = (TextView)dialogView.findViewById(R.id.tv_result_count);
		resultTime.setText("Total time spent: " + String.valueOf(duringTime) + "ms");
		resultCount.setText("Total: " + String.valueOf(results.length));

		final ArrayList<Map<String, String>> resultMapList = new ArrayList<>();
		for (int i = 0; i < results.length; i++){
			Map<String, String> temp = new HashMap<>();
			temp.put("Barcode", String.valueOf(i + 1));
			temp.put("Format", DBRUtil.getCodeFormat(results[i].barcodeFormat + ""));
			temp.put("Text", results[i].barcodeText);
			resultMapList.add(temp);
		}
		SimpleAdapter resultAdapter = new SimpleAdapter(MainActivity.this, resultMapList, R.layout.item_listview_result_list,
				new String[]{"Barcode", "Format", "Text"}, new int[]{R.id.tv_result_index, R.id.tv_result_format, R.id.tv_result_text});
		resultAdapter.notifyDataSetChanged();
		resultListView.setAdapter(resultAdapter);
		resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String o = resultMapList.get(position).get("Text");
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clipData = ClipData.newPlainText("", o);
				clipboardManager.setPrimaryClip(clipData);
				Toast.makeText(MainActivity.this, "Text has been copied to clipboard.", Toast.LENGTH_SHORT).show();
			}
		});
		final AlertDialog resultBuilder = new AlertDialog.Builder(MainActivity.this).create();
		resultBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
			  detectStart = true;
			}
		});
		resultBuilder.setView(dialogView);
		resultBuilder.show();
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringmode = audioManager.getRingerMode();
		if (ringmode == AudioManager.RINGER_MODE_NORMAL) {
			if (beepSoundEnabled) {
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer = MediaPlayer.create(this, R.raw.beepsound);
				mediaPlayer.start();
				Vibrator vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
			} else {
				Vibrator vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
			}
		} else {
			Vibrator vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(500);
		}
	}

	private void obtainPreviewScale() {
		if (hudView.getWidth() == 0 || hudView.getHeight() == 0) {
			return;
		}
		fotoapparat.getCurrentParameters().whenAvailable(new Function1<CameraParameters, Unit>() {
			@Override
			public Unit invoke(CameraParameters cameraParameters) {
				previewSize = cameraParameters.getPreviewResolution();
				previewScale = frameUtil.calculatePreviewScale(previewSize, hudView.getWidth(), hudView.getHeight());
				return Unit.INSTANCE;
			}
		});
	}

	private void drawDocumentBox(ArrayList<RectPoint[]> rectCoord) {
		hudView.clear();
		if (!isSingleMode) {
			hudView.setBoundaryPoints(rectCoord);
		}
		hudView.invalidate();
		isDetected = true;
	}

	private void fulFillRecentList(TextResult[] result) {
		recentCodeList.clear();
		//dragView.setText(result[0].barcodeText);
		for (TextResult aResult1 : result) {
			Map<String, String> recentCodeItem = new HashMap<>();
			recentCodeItem.put("format", DBRUtil.getCodeFormat(aResult1.barcodeFormat + ""));
			recentCodeItem.put("text", aResult1.barcodeText);
			recentCodeList.add(recentCodeItem);
		}
		simpleAdapter.notifyDataSetChanged();
	}

	private void setupFotoapparat() {
		fotoapparat = Fotoapparat
				.with(this)
				.into(cameraView)
				.previewScaleType(ScaleType.CenterCrop)
				.lensPosition(back())
				.frameProcessor(new CodeFrameProcesser())
				.build();
	}

	public void shootSound() {
		MediaActionSound sound = new MediaActionSound();
		sound.play(MediaActionSound.SHUTTER_CLICK);
	}

	private void initUI() {
		slidingDrawer.setDragView(dragView);
		simpleAdapter = new SimpleAdapter(MainActivity.this, recentCodeList,
				R.layout.item_listview_recent_code, new String[]{"format", "text"}, new int[]{R.id.tv_code_format, R.id.tv_code_text});
		ivPull.setImageResource(R.drawable.arrow_up);
		lvBarcodeList.setAdapter(simpleAdapter);
		btnCapture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnCapture.setEnabled(false);
				shootSound();
				PhotoResult photoResult = fotoapparat.takePicture();
				final String photoName = System.currentTimeMillis() + "";
				photoResult.saveToFile(new File(getExternalFilesDir("photos"), photoName + ".jpg"
				)).whenDone(new WhenDoneListener<Unit>() {
					@Override
					public void whenDone(@Nullable Unit it) {
						Logger.d("save img done~!");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								btnCapture.setEnabled(true);
							}
						});
						Intent intent = new Intent(MainActivity.this, HistoryItemDetailActivity.class);
						intent.putExtra("page_type", 0);
						intent.putExtra("photoname", photoName);
						startActivity(intent);
					}
				});
			}
		});
		setupSlidingDrawer();
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				detectStart = true;
			}
		});
		btnFinish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				detectStart = false;
				Intent intent = new Intent(MainActivity.this, HistoryItemDetailActivity.class);
				intent.putExtra("page_type", 3);
				startActivity(intent);
			}
		});
		btnDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
				intent.putExtra("templateType", templateType);
				startActivity(intent);
			}
		});
	}

	private void switchToMulti() {
		isSingleMode = false;
		if ("OverlapSetting".equals(templateType)) {
			slidingDrawer.setVisibility(View.VISIBLE);
			mScanCount.setVisibility(View.VISIBLE);
		} else {
			slidingDrawer.setVisibility(View.GONE);
			mScanCount.setVisibility(View.GONE);
		}
		btnCapture.setVisibility(View.GONE);
		lineDone.setVisibility(View.VISIBLE);
	}

	private void switchToSingle() {
		isSingleMode = true;
		hudView.clear();
		hudView.invalidate();
		lineDone.setVisibility(View.GONE);
		slidingDrawer.setVisibility(View.GONE);
		mScanCount.setVisibility(View.GONE);
		btnCapture.setVisibility(View.VISIBLE);
	}

	private void choicePhotoWrapper() {
		BGAPhotoHelper photoHelper = new BGAPhotoHelper(new File(Environment.getExternalStorageDirectory(), "DBRDemo"));
		startActivityForResult(photoHelper.getChooseSystemGalleryIntent(), REQUEST_CHOOSE_PHOTO);
	}

	private void goToSetting() {
		Intent intent = new Intent(MainActivity.this, SettingActivity.class);
		intent.putExtra("templateType", templateType);
		startActivityForResult(intent, REQUEST_SETTING);
	}

	class CodeFrameProcesser implements FrameProcessor {
		@Override
		public void process(@NonNull Frame frame) {
			try {
				if (isDetected && !isDrawerExpand && !isSingleMode && detectStart) {
					isDetected = false;
					if (previewSize == null) {
						Message obtainPreviewMsg = handler.obtainMessage();
						obtainPreviewMsg.what = OBTAIN_PREVIEW_SIZE;
						handler.sendMessage(obtainPreviewMsg);
					}
					YuvImage yuvImage = new YuvImage(frame.getImage(), ImageFormat.NV21,
							frame.getSize().width, frame.getSize().height, null);
					wid = frame.getSize().width;
					hgt = frame.getSize().height;
					startDetectTime = System.currentTimeMillis();
					result = reader.decodeBuffer(yuvImage.getYuvData(), wid, hgt,
							yuvImage.getStrides()[0], EnumImagePixelFormat.IPF_NV21, "Custom");
					endDetectTime = System.currentTimeMillis();
					duringTime = endDetectTime - startDetectTime;
					ArrayList<TextResult> resultArrayList = new ArrayList<>();
					for (int i = 0; i < result.length; i++) {
						if (result[i] != null && result[i].localizationResult.extendedResultArray[0].confidence > 50) {
							resultArrayList.add(result[i]);
						}
					}
					result = resultArrayList.toArray(new TextResult[resultArrayList.size()]);
					ArrayList<TextResult> textResults = new ArrayList<>();
					for (int i = 0; i < result.length; i++){
						boolean flag = false;
						for (int j = 0; j < textResults.size(); j++){
							if (result[i].barcodeFormat == textResults.get(j).barcodeFormat && result[i].barcodeText.equals(textResults.get(j).barcodeText)){
								flag = true;
							}
						}
						if (!flag){
							textResults.add(result[i]);
						}
					}
					result = textResults.toArray(new TextResult[textResults.size()]);
					LocalizationResult[] r = reader.getAllLocalizationResults();
					//Message coordMessage = handler.obtainMessage();
					Message message = handler.obtainMessage();
					if (result != null && result.length > 0) {
						ArrayList<RectPoint[]> rectCoord = frameUtil.handlePoints(result, previewScale, hgt, wid);
						if ("OverlapSetting".equals(templateType)) {
							if (frameTime == 0) {
								yuvInfo = new YuvInfo();
								yuvInfo.cacheName = System.currentTimeMillis() + "";
								yuvInfo.yuvImage = yuvImage;
								yuvInfo.textResult = result;
								yuvInfoList.add(yuvInfo);
								frameTime++;
							} else if (frameTime == 1) {
								yuvInfo = new YuvInfo();
								yuvInfo.textResult = result;
								yuvInfo.yuvImage = yuvImage;
								yuvInfo.cacheName = System.currentTimeMillis() + "";
								if (yuvInfoList.size() == 1) {
									yuvInfoList.add(yuvInfo);
								} else {
									yuvInfoList.set(1, yuvInfo);
								}
								CoordsMapResult coordsMapResult = AfterProcess.coordsMap
										(yuvInfoList.get(0).textResult, yuvInfoList.get(1).textResult, wid, hgt);
								if (coordsMapResult != null) {
									LocalizationResult localizationResult;
									TextResult textResult;
									switch (coordsMapResult.basedImg) {
										case 0:
											//coordMessage.obj = frameUtil.handlePoints(yuvInfoList.get(1).textResult, previewScale, hgt, wid);
											message.obj = yuvInfoList.get(1).textResult;
											if (readySaveYuvInfo != null){
												coordMapAndSave(readySaveYuvInfo, yuvInfoList.get(0));
												readySaveYuvInfo = null;
											} else {
												handleImage(yuvInfoList.get(0), null);
											}
											yuvInfoList.set(0, yuvInfoList.get(1));
											break;
										case 1:
											TextResult[] newResultBase1 = new TextResult[coordsMapResult.resultArr.length];
											for (int i = 0; i < coordsMapResult.resultArr.length; i++) {

												localizationResult = new LocalizationResult();
												localizationResult.resultPoints = coordsMapResult.resultArr[i].pts;
												textResult = new TextResult();
												textResult.localizationResult = localizationResult;
												textResult.barcodeText = coordsMapResult.resultArr[i].barcodeText;
												textResult.barcodeBytes = coordsMapResult.resultArr[i].barcodeBytes;
												textResult.barcodeFormat = coordsMapResult.resultArr[i].format;
												newResultBase1[i] = textResult;
											}
											if (coordsMapResult.isAllCodeMapped) {
												flag++;
												if (flag > 4){
													message.obj = yuvInfoList.get(1).textResult;
													flag = 0;
													if (readySaveYuvInfo != null){
														coordMapAndSave(readySaveYuvInfo, yuvInfoList.get(0));
														readySaveYuvInfo = null;
													} else {
														handleImage(yuvInfoList.get(0), null);
													}
													readySaveYuvInfo = yuvInfoList.get(0);
													yuvInfoList.set(0, yuvInfoList.get(1));
												} else {
													yuvInfoList.get(0).textResult = newResultBase1;
													message.obj = newResultBase1;
													//coordMessage.obj = frameUtil.handlePoints(newResultBase1, previewScale, hgt, wid);
												}
											} else {
												yuvInfoList.get(0).textResult = newResultBase1;
												message.obj = yuvInfoList.get(1).textResult;
												if (readySaveYuvInfo != null){
													coordMapAndSave(readySaveYuvInfo, yuvInfoList.get(0));
													readySaveYuvInfo = null;
												} else {
													handleImage(yuvInfoList.get(0), null);
												}
												//coordMessage.obj = frameUtil.handlePoints(yuvInfoList.get(1).textResult, previewScale, hgt, wid);
												yuvInfoList.set(0, yuvInfoList.get(1));
											}
											break;
										case 2:
											TextResult[] newResultBase2 = new TextResult[coordsMapResult.resultArr.length];
											for (int i = 0; i < coordsMapResult.resultArr.length; i++) {
												localizationResult = new LocalizationResult();
												localizationResult.resultPoints = coordsMapResult.resultArr[i].pts;
												textResult = new TextResult();
												textResult.localizationResult = localizationResult;
												textResult.barcodeText = coordsMapResult.resultArr[i].barcodeText;
												textResult.barcodeBytes = coordsMapResult.resultArr[i].barcodeBytes;
												textResult.barcodeFormat = coordsMapResult.resultArr[i].format;
												newResultBase2[i] = textResult;
											}
											message.obj = newResultBase2;
											if (coordsMapResult.isAllCodeMapped) {
												yuvInfoList.get(1).textResult = newResultBase2;
												//coordMessage.obj = frameUtil.handlePoints(newResultBase2, previewScale, hgt, wid);
												yuvInfoList.set(0, yuvInfoList.get(1));
											} else {
												yuvInfoList.get(1).textResult = newResultBase2;
												if (readySaveYuvInfo != null){
													coordMapAndSave(readySaveYuvInfo, yuvInfoList.get(0));
													readySaveYuvInfo = null;
												} else {
													handleImage(yuvInfoList.get(0), null);
												}
												//coordMessage.obj = frameUtil.handlePoints(newResultBase2, previewScale, hgt, wid);
												yuvInfoList.set(0, yuvInfoList.get(1));
											}
											break;
										case -1:
											//coordMessage.obj = frameUtil.handlePoints(yuvInfoList.get(1).textResult, previewScale, hgt, wid);
											message.obj = yuvInfoList.get(1).textResult;
											yuvInfoList.set(0, yuvInfoList.get(1));
											break;
										case -2:
											message.obj = yuvInfoList.get(1).textResult;
											if (readySaveYuvInfo != null){
												coordMapAndSave(readySaveYuvInfo, yuvInfoList.get(0));
												readySaveYuvInfo = null;
											} else {
												handleImage(yuvInfoList.get(1), null);
											}
											yuvInfoList.set(0, yuvInfoList.get(1));
										default:
											break;
									}
								}
							}
						}
						if ("GeneralSetting".equals(templateType) || ("MultiBestSetting").equals(templateType)) {
							Message resultMessage = handler.obtainMessage();
							resultMessage.what = BARCODE_RESULT;
							resultMessage.obj = result;
							handler.sendMessage(resultMessage);
							detectStart = false;
							yuvInfo = new YuvInfo();
							yuvInfo.textResult = result;
							yuvInfo.yuvImage = yuvImage;
							yuvInfo.cacheName = System.currentTimeMillis() + "";
							handleImage(yuvInfo, null);
						}
						//coordMessage.what = BARCODE_RECT_COORD;
						//handler.sendMessage(coordMessage);
						message.what = DETECT_BARCODE;
						handler.sendMessage(message);
					} else {
						message.obj = null;
						message.what = DETECT_BARCODE;
						handler.sendMessage(message);
					}

				} else {
					isDetected = true;
				}
			} catch (BarcodeReaderException e) {
				e.printStackTrace();
			}
		}
		private void coordMapAndSave(YuvInfo yuvInfo1, YuvInfo yuvInfo2){
			CoordsMapResult result = AfterProcess.coordsMap(yuvInfo1.textResult, yuvInfo2.textResult, wid, hgt);
			switch (result.basedImg) {
				case 1:
					if (!result.isAllCodeMapped){
						handleImage(yuvInfo2, null);
					}
					break;
				case 2:
					if (result.isAllCodeMapped){
						handleImage(yuvInfo2, yuvInfo1.cacheName);
					} else {
						handleImage(yuvInfo2, null);
					}
					break;
				case 0:
					handleImage(yuvInfo2, null);
					break;
				case -1:
					handleImage(yuvInfo2, yuvInfo1.cacheName);
					break;
				case -2:
					handleImage(yuvInfo2, null);
					break;
				default:
					break;
			}
		}
		private void deleteErroCache(String name) {
			if (name == null) {
				return;
			}
			List<DBRImage> erroImage = LitePal.where("fileName = ?", name).find(DBRImage.class);
			if (erroImage != null && erroImage.size() > 0) {
				File previewFile = new File(erroImage.get(0).getCodeImgPath());
				if (previewFile.exists()) {
					previewFile.delete();
					LitePal.deleteAll(DBRImage.class, "fileName = ?", name);
				}
			}
		}
		private void handleImage(final YuvInfo yuvInfo, final String deleCacheName) {
			endDetectTime = System.currentTimeMillis();
			threadManager.execute(new Runnable() {
				@Override
				public void run() {
					try {
						deleteErroCache(deleCacheName);
						long startSaveFile = System.currentTimeMillis();
						YuvImage newYuv = new YuvImage(FrameUtil.rotateYUVDegree90(yuvInfo.yuvImage.getYuvData(),
								yuvInfo.yuvImage.getWidth(), yuvInfo.yuvImage.getHeight()), ImageFormat.NV21, yuvInfo.yuvImage.getHeight(), yuvInfo.yuvImage.getWidth(), null);
						File previewFile = new File(path + "/" + yuvInfo.cacheName + ".jpg");
						if (!previewFile.exists()) {
							previewFile.getParentFile().mkdirs();
							previewFile.createNewFile();
						}
						FileOutputStream fileOutputStream = new FileOutputStream(previewFile);
						newYuv.compressToJpeg(new Rect(0, 0, newYuv.getWidth(), newYuv.getHeight()), 100, fileOutputStream);
						fileOutputStream.flush();
						fileOutputStream.close();
						ArrayList<String> codeFormatList = new ArrayList<>();
						ArrayList<String> codeTextList = new ArrayList<>();
						/*ArrayList<TextResult> textResults = new ArrayList<>();
						for (int i = 0; i < yuvInfo.textResult.length; i++){
							boolean flag = false;
							for (int j = 0; j < textResults.size(); j++){
								if (yuvInfo.textResult[i].barcodeFormat == textResults.get(j).barcodeFormat && yuvInfo.textResult[i].barcodeText.equals(textResults.get(j).barcodeText)){
									flag = true;
								}
							}
							if (!flag){
								textResults.add(yuvInfo.textResult[i]);
							}
						}
						yuvInfo.textResult = textResults.toArray(new TextResult[textResults.size()]);*/
						ArrayList<RectPoint[]> pointList = frameUtil.rotatePoints(yuvInfo.textResult,
								yuvInfo.yuvImage.getHeight(), yuvInfo.yuvImage.getWidth());
						for (TextResult result1 : yuvInfo.textResult) {
							codeFormatList.add(result1.barcodeFormat + "");
							codeTextList.add(result1.barcodeText);
						}
						DBRImage dbrImage = new DBRImage();
						dbrImage.setFileName(yuvInfo.cacheName);
						dbrImage.setCodeFormat(codeFormatList);
						dbrImage.setCodeText(codeTextList);
						dbrImage.setCodeImgPath(path + "/" + yuvInfo.cacheName + ".jpg");
						RectCoordinate rectCoordinate = new RectCoordinate();
						rectCoordinate.setRectCoord(pointList);
						String rectCoord = LoganSquare.serialize(rectCoordinate);
						dbrImage.setRectCoord(rectCoord);
						dbrImage.setDecodeTime(duringTime);
						dbrImage.setTemplateType(templateType);
						dbrImage.save();
						long endSaveFile = System.currentTimeMillis();
						//Logger.d("save file time : " + (endSaveFile - startSaveFile));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}

