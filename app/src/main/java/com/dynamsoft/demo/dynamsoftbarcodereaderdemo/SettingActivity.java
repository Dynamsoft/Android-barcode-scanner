package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.DBRSetting;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.DBRCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {
	private DBRSetting mSetting;
	private DBRCache mSettingCache;
	private DBRSetting.ImageParameter mImageParameter;
	private String templateType;
	private List<String> one2Ten = new ArrayList<>();
	private List<String> colourImageConvertMode = new ArrayList<>();
	private List<String> barcodeInvertMode = new ArrayList<>();
	private ArrayList<String> tempFormats;
	private ArrayAdapter<String> one2tenSpinnerAdapter;
	private ArrayAdapter<String> isEnableSpinnerAdapter;
	private ArrayAdapter<String> barcodeInvertModeSpinnerAdapter;
	private ArrayAdapter<String> colourImageConvertModeSpinnerAdapter;
	private final int REQUEST_ONED_SETTING = 0x0001;
	private final int REQUEST_ALGORITHM_SETTING = 0x0002;
	private final int RESPONSE_ONED_SETTING = 0x0001;
	private final int RESPONSE_ALGORITHM_SETTING = 0x0002;
	private final int RESPONSE_GENERAL_SETTING = 0x0001;
	private final int RESPONSE_MULTIBEST_SETTING = 0X0002;
	private final int RESPONSE_MULTIBAL_SETTING = 0X0003;
	@BindView(R.id.setoned)
	ImageView ivSetOned;
	@BindView(R.id.ckbpdf417)
	CheckBox mPDF417;
	@BindView(R.id.ckbqrcode)
	CheckBox mQRCode;
	@BindView(R.id.ckbdatamatrix)
	CheckBox mDataMatrix;
	@BindView(R.id.tv_expected_barcode_count)
	TextView tvExpectedBarcodeCount;
	@BindView(R.id.et_expected_barcode_count)
	EditText etExpectedBarcodeCount;
	@BindView(R.id.tv_timeout)
	TextView tvTimeout;
	@BindView(R.id.et_timeout)
	EditText etTimeout;
	@BindView(R.id.tv_scale_down_threshold)
	TextView tvScaleDownThreshold;
	@BindView(R.id.et_scale_down_threshold)
	EditText etScaleDownThreshold;
	@BindView(R.id.tv_binarization_block_size)
	TextView tvBinarizationBlockSize;
	@BindView(R.id.et_binarization_block_size)
	EditText etBinarizationBlockSize;
	@BindView(R.id.tv_max_dimof_full_image_as_barcode_zone)
	TextView tvMaxDimofFullImageAsBarcodeZone;
	@BindView(R.id.et_max_dimof_full_image_as_barcode_zone)
	EditText etMaxDimofFullImageAsBarcodeZone;
	@BindView(R.id.tv_max_barcode_count)
	TextView tvMaxBarcodeCount;
	@BindView(R.id.et_max_barcode_count)
	EditText etMaxBarcodeCount;
	@BindView(R.id.sc_enable_fill_binary_vacancy)
	SwitchCompat scEnableFillBinaryVacancy;
	@BindView(R.id.sc_region_predetection_mode)
	SwitchCompat scRegionPredetectionMode;
	@BindView(R.id.sc_text_filter_mode)
	SwitchCompat scTextFilterMode;
	@BindView(R.id.sp_deblur_level)
	Spinner spDeblurLevel;
	@BindView(R.id.sp_anti_damage_level)
	Spinner spAntiDamageLevel;
	@BindView(R.id.sp_gray_equalization_sensitivity)
	Spinner spGrayEqualizationSensitivity;
	@BindView(R.id.sp_texture_detection_sensitivity)
	Spinner spTextureDetectionSensitivity;
	@BindView(R.id.sp_barcode_invert_mode)
	Spinner spBarcodeInvertMode;
	@BindView(R.id.sp_colour_image_convert_mode)
	Spinner spColourImageConvertMode;
	@Override
	protected int getLayoutId() {
		return R.layout.activity_setting;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		ButterKnife.bind(this);
		setToolbarBackgroud("#000000");
		setToolbarNavIcon(R.drawable.ic_action_back_dark);
		setToolbarTitle("Setting");
		setToolbarTitleColor("#ffffff");
		initSpinner();
		initSetting();
		etExpectedBarcodeCount.setOnEditorActionListener(onEditFinish);
		etTimeout.setOnEditorActionListener(onEditFinish);
		etExpectedBarcodeCount.setOnEditorActionListener(onEditFinish);
		etBinarizationBlockSize.setOnEditorActionListener(onEditFinish);
		etMaxDimofFullImageAsBarcodeZone.setOnEditorActionListener(onEditFinish);
		etMaxBarcodeCount.setOnEditorActionListener(onEditFinish);
		etScaleDownThreshold.setOnEditorActionListener(onEditFinish);
		scEnableFillBinaryVacancy.setOnCheckedChangeListener(onSCCheckedChange);
		scRegionPredetectionMode.setOnCheckedChangeListener(onSCCheckedChange);
		scTextFilterMode.setOnCheckedChangeListener(onSCCheckedChange);
		mDataMatrix.setOnCheckedChangeListener(onCKBCheckedChange);
		mQRCode.setOnCheckedChangeListener(onCKBCheckedChange);
		mPDF417.setOnCheckedChangeListener(onCKBCheckedChange);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_share).setVisible(false);
		menu.findItem(R.id.menu_capture).setVisible(false);
		menu.findItem(R.id.menu_file).setVisible(false);
		menu.findItem(R.id.menu_scanning).setVisible(false);
		menu.findItem(R.id.menu_Setting).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	public void onClicked(View view) {
		switch (view.getId()) {
			case R.id.setoned:
				Intent intent = new Intent(SettingActivity.this, BarcodeTypeActivity.class);
				intent.putExtra("DBRSetting", mSetting);
				startActivityForResult(intent, REQUEST_ONED_SETTING);
				break;
			case R.id.setalgorithm:
				Intent intent1 = new Intent(SettingActivity.this, AlgorithmSettingActivity.class);
				intent1.putExtra("DBRSetting", mSetting);
				startActivityForResult(intent1, REQUEST_ALGORITHM_SETTING);
				break;
			case R.id.tv_expected_barcode_count:
				etExpectedBarcodeCount.setText(tvExpectedBarcodeCount.getText());
				tvExpectedBarcodeCount.setVisibility(View.GONE);
				etExpectedBarcodeCount.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_timeout:
				etTimeout.setText(tvTimeout.getText());
				tvTimeout.setVisibility(View.GONE);
				etTimeout.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_scale_down_threshold:
				etScaleDownThreshold.setText(tvScaleDownThreshold.getText());
				tvScaleDownThreshold.setVisibility(View.GONE);
				etScaleDownThreshold.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_binarization_block_size:
				etBinarizationBlockSize.setText(tvBinarizationBlockSize.getText());
				tvBinarizationBlockSize.setVisibility(View.GONE);
				etBinarizationBlockSize.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_max_dimof_full_image_as_barcode_zone:
				etMaxDimofFullImageAsBarcodeZone.setText(tvMaxDimofFullImageAsBarcodeZone.getText());
				tvMaxDimofFullImageAsBarcodeZone.setVisibility(View.GONE);
				etMaxDimofFullImageAsBarcodeZone.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_max_barcode_count:
				etMaxBarcodeCount.setText(tvMaxBarcodeCount.getText());
				tvMaxBarcodeCount.setVisibility(View.GONE);
				etMaxBarcodeCount.setVisibility(View.VISIBLE);
				break;
			default:
				break;
		}
	}
	private void initSpinner(){
		barcodeInvertMode.add("DarkOnLight");
		barcodeInvertMode.add("LightOnDark");
		barcodeInvertModeSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, barcodeInvertMode);
		barcodeInvertModeSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		colourImageConvertMode.add("Auto");
		colourImageConvertMode.add("Grayscale");
		colourImageConvertModeSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, colourImageConvertMode);
		colourImageConvertModeSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		for(int i = 0; i < 10; i++){
			one2Ten.add(String.valueOf(i));
		}
		one2tenSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, one2Ten);
		one2tenSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		spDeblurLevel.setAdapter(one2tenSpinnerAdapter);
		spAntiDamageLevel.setAdapter(one2tenSpinnerAdapter);
		spGrayEqualizationSensitivity.setAdapter(one2tenSpinnerAdapter);
		spTextureDetectionSensitivity.setAdapter(one2tenSpinnerAdapter);
		spBarcodeInvertMode.setAdapter(barcodeInvertModeSpinnerAdapter);
		spColourImageConvertMode.setAdapter(colourImageConvertModeSpinnerAdapter);

		spDeblurLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mImageParameter.setDeblurLevel(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spAntiDamageLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mImageParameter.setAntiDamageLevel(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spGrayEqualizationSensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mImageParameter.setGrayEqualizationSensitivity(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spTextureDetectionSensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mImageParameter.setTextureDetectionSensitivity(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spBarcodeInvertMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position == 0){
					mImageParameter.setBarcodeInvertMode(barcodeInvertMode.get(0));
				}
				else{
					mImageParameter.setBarcodeInvertMode(barcodeInvertMode.get(1));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spColourImageConvertMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position ==0){
					mImageParameter.setColourImageConvertMode(colourImageConvertMode.get(0));
				}
				else {
					mImageParameter.setColourImageConvertMode(colourImageConvertMode.get(1));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}
	private void initSetting(){
		mSettingCache = DBRCache.get(this, "SettingCache");
		templateType = getIntent().getStringExtra("templateType");
		try {
			mSetting = LoganSquare.parse(mSettingCache.getAsString("GeneralSetting"), DBRSetting.class);
			mImageParameter = mSetting.getImageParameter();
			tvExpectedBarcodeCount.setText(String.valueOf(mImageParameter.getExpectedBarcodesCount()));
			tvTimeout.setText(String.valueOf(mImageParameter.getTimeout()));
			spDeblurLevel.setSelection(mImageParameter.getDeblurLevel());
			spAntiDamageLevel.setSelection(mImageParameter.getAntiDamageLevel());
			scTextFilterMode.setChecked(mImageParameter.isTextFilterMode());
			scRegionPredetectionMode.setChecked(mImageParameter.isRegionPredetectionMode());
			tvScaleDownThreshold.setText(String.valueOf(mImageParameter.getScaleDownThreshold()));
			if (mImageParameter.getColourImageConvertMode().equals(colourImageConvertMode.get(0))){
				spColourImageConvertMode.setSelection(0);
			}else {
				spColourImageConvertMode.setSelection(1);
			}
			if (mImageParameter.getBarcodeInvertMode().equals(barcodeInvertMode.get(0))){
				spBarcodeInvertMode.setSelection(0);
			}else {
				spBarcodeInvertMode.setSelection(1);
			}
			spGrayEqualizationSensitivity.setSelection(mImageParameter.getGrayEqualizationSensitivity());
			spTextureDetectionSensitivity.setSelection(mImageParameter.getTextureDetectionSensitivity());
			tvBinarizationBlockSize.setText(String.valueOf(mImageParameter.getBinarizationBlockSize()));

			tvMaxDimofFullImageAsBarcodeZone.setText(String.valueOf(mImageParameter.getMaxDimOfFullImageAsBarcodeZone()));
			tvMaxBarcodeCount.setText(String.valueOf(mImageParameter.getMaxBarcodesCount()));
			scEnableFillBinaryVacancy.setChecked(mImageParameter.isEnableFillBinaryVacancy());
			ArrayList<String> formats = mImageParameter.getBarcodeFormatIds();
			if (formats.contains("PDF417")) {
				mPDF417.setChecked(true);
			} else {
				mPDF417.setChecked(false);
			}
			if (formats.contains("QR_CODE")) {
				mQRCode.setChecked(true);
			} else {
				mQRCode.setChecked(false);
			}
			if (formats.contains("DATAMATRIX")) {
				mDataMatrix.setChecked(true);
			} else {
				mDataMatrix.setChecked(false);
			}
			/*if (formats.contains("CODE_39") && formats.contains("CODE_128") &&
			    	formats.contains("CODE_93") && formats.contains("CODABAR") &&
					formats.contains("ITF") && formats.contains("EAN_13") &&
					formats.contains("EAN_8") && formats.contains("UPC_E") &&
					formats.contains("INDUSTRIAL_25") && formats.contains("UPC_A")){
				mOned.setChecked(true);
			} else {
				mOned.setChecked(false);
			}*/
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
	@Override
	public void onBackPressed(){
		mSetting.setImageParameter(mImageParameter);
		try {
			if ("general".equals(templateType)) {
				mSettingCache.put("GeneralSetting", LoganSquare.serialize(mSetting));
				setResult(RESPONSE_GENERAL_SETTING);
			}
			if ("multiBest".equals(templateType)) {
				mSettingCache.put("MultiBestSetting", LoganSquare.serialize(mSetting));
				setResult(RESPONSE_MULTIBEST_SETTING);
			}
			if ("multiBal".equals(templateType)) {
				mSettingCache.put("MultiBalSetting", LoganSquare.serialize(mSetting));
				setResult(RESPONSE_MULTIBAL_SETTING);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		super.onBackPressed();
	}
	SwitchCompat.OnCheckedChangeListener onSCCheckedChange = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()){
				case R.id.sc_enable_fill_binary_vacancy:
					mImageParameter.setEnableFillBinaryVacancy(scEnableFillBinaryVacancy.isChecked());
					break;
				case R.id.sc_region_predetection_mode:
					mImageParameter.setRegionPredetectionMode(scRegionPredetectionMode.isChecked());
					break;
				case R.id.sc_text_filter_mode:
					mImageParameter.setTextFilterMode(scTextFilterMode.isChecked());
					break;
				default:
					break;
			}

		}
	};
	CheckBox.OnCheckedChangeListener onCKBCheckedChange = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()){
				/*case R.id.ckboned:
					if (mOned.isChecked()) {
						tempFormats = mSetting.getBarcodeFormatIds();
						tempFormats.addAll(oneDFormats);
						mSetting.setBarcodeFormatIds(tempFormats);
					}else {
						tempFormats = mSetting.getBarcodeFormatIds();
						tempFormats.removeAll(oneDFormats);
						mSetting.setBarcodeFormatIds(tempFormats);
					}
					break;*/
				case R.id.ckbpdf417:
					if (mPDF417.isChecked()){
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.add("PDF417");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}else {
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.remove("PDF417");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}
					break;
				case R.id.ckbqrcode:
					if (mQRCode.isChecked()) {
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.add("QR_CODE");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}else {
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.remove("QR_CODE");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}
					break;
				case R.id.ckbdatamatrix:
					if (mDataMatrix.isChecked()) {
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.add("DATAMATRIX");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}else {
						tempFormats = mImageParameter.getBarcodeFormatIds();
						tempFormats.remove("DATAMATRIX ");
						mImageParameter.setBarcodeFormatIds(tempFormats);
					}
					break;
				default:
					break;
			}
		}
	};
	EditText.OnEditorActionListener onEditFinish = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.ACTION_DOWN) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			int tempValue;
				switch (v.getId()) {
					case R.id.et_expected_barcode_count:
						try {
							imm.hideSoftInputFromWindow(etExpectedBarcodeCount.getWindowToken(), 0);
							tempValue = Integer.parseInt(etExpectedBarcodeCount.getText().toString());
							if (tempValue >= 0 && tempValue <= 100) {
								mImageParameter.setExpectedBarcodesCount(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 100]", Toast.LENGTH_LONG).show();
								mImageParameter.setExpectedBarcodesCount(0);
							}
						}
						catch (Exception ex) {
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 100]", Toast.LENGTH_LONG).show();
							mImageParameter.setExpectedBarcodesCount(0);
						}
						tvExpectedBarcodeCount.setText(String.valueOf(mImageParameter.getExpectedBarcodesCount()));
						etExpectedBarcodeCount.setVisibility(View.GONE);
						tvExpectedBarcodeCount.setVisibility(View.VISIBLE);
						break;
					case R.id.et_timeout:
						try {
							imm.hideSoftInputFromWindow(etTimeout.getWindowToken(), 0);
							tempValue = Integer.parseInt(etTimeout.getText().toString());
							if (tempValue >= 0) {
								mImageParameter.setTimeout(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 0x7fffffff]", Toast.LENGTH_LONG).show();
								mImageParameter.setTimeout(10000);
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 0x7fffffff]", Toast.LENGTH_LONG).show();
							mImageParameter.setTimeout(10000);
						}
						tvTimeout.setText(String.valueOf(mImageParameter.getTimeout()));
						etTimeout.setVisibility(View.GONE);
						tvTimeout.setVisibility(View.VISIBLE);
						break;
					case R.id.et_scale_down_threshold:
						try {
							imm.hideSoftInputFromWindow(etScaleDownThreshold.getWindowToken(), 0);
							tempValue = Integer.parseInt(etScaleDownThreshold.getText().toString());
							if (tempValue >= 512) {
								mImageParameter.setScaleDownThreshold(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [512, 0x7fffffff]", Toast.LENGTH_LONG).show();
								mImageParameter.setScaleDownThreshold(2300);
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [512, 0x7fffffff]", Toast.LENGTH_LONG).show();
							mImageParameter.setScaleDownThreshold(2300);
						}
						tvScaleDownThreshold.setText(String.valueOf(mImageParameter.getScaleDownThreshold()));
						etScaleDownThreshold.setVisibility(View.GONE);
						tvScaleDownThreshold.setVisibility(View.VISIBLE);
						break;
					case R.id.et_binarization_block_size:
						try {
							imm.hideSoftInputFromWindow(etBinarizationBlockSize.getWindowToken(), 0);
							tempValue = Integer.parseInt(etBinarizationBlockSize.getText().toString());
							if (tempValue >= 0 && tempValue <= 1000){
								mImageParameter.setBinarizationBlockSize(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 1000]", Toast.LENGTH_LONG).show();
								mImageParameter.setBinarizationBlockSize(0);
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [0, 1000]", Toast.LENGTH_LONG).show();
							mImageParameter.setBinarizationBlockSize(0);
						}
						tvBinarizationBlockSize.setText(String.valueOf(mImageParameter.getBinarizationBlockSize()));
						etBinarizationBlockSize.setVisibility(View.GONE);
						tvBinarizationBlockSize.setVisibility(View.VISIBLE);
						break;
					case R.id.et_max_dimof_full_image_as_barcode_zone:
						try {
							imm.hideSoftInputFromWindow(etMaxDimofFullImageAsBarcodeZone.getWindowToken(), 0);
							tempValue = Integer.parseInt(etMaxDimofFullImageAsBarcodeZone.getText().toString());
							if (tempValue >= 262144){
								mImageParameter.setMaxDimOfFullImageAsBarcodeZone(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [262144, 0x7fffffff]", Toast.LENGTH_LONG).show();
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [262144, 0x7fffffff]", Toast.LENGTH_LONG).show();
						}
						tvMaxDimofFullImageAsBarcodeZone.setText(String.valueOf(mImageParameter.getMaxDimOfFullImageAsBarcodeZone()));
						etMaxDimofFullImageAsBarcodeZone.setVisibility(View.GONE);
						tvMaxDimofFullImageAsBarcodeZone.setVisibility(View.VISIBLE);
						break;
					case R.id.et_max_barcode_count:
						try{
							imm.hideSoftInputFromWindow(etMaxBarcodeCount.getWindowToken(), 0);
							tempValue = Integer.parseInt(etMaxBarcodeCount.getText().toString());
							if (tempValue >= 1){
								mImageParameter.setMaxBarcodesCount(tempValue);
							} else {
								Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [1, 0x7fffffff]", Toast.LENGTH_LONG).show();
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							Toast.makeText(SettingActivity.this, "Input Invalid! Legal value: [1, 0x7fffffff]", Toast.LENGTH_LONG).show();
						}
						tvMaxBarcodeCount.setText(String.valueOf(mImageParameter.getMaxBarcodesCount()));
						etMaxBarcodeCount.setVisibility(View.GONE);
						tvMaxBarcodeCount.setVisibility(View.VISIBLE);
					default:
						break;
				}
				return true;
			}
			return false;
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUEST_ONED_SETTING && resultCode == RESPONSE_ONED_SETTING)) {
			mSetting = (DBRSetting) data.getSerializableExtra("OneDSetting");
			mImageParameter = mSetting.getImageParameter();
			/*ArrayList<String> formats = mSetting.getBarcodeFormatIds();
			if (formats.contains("CODE_39") && formats.contains("CODE_128") &&
					formats.contains("CODE_93") && formats.contains("CODABAR") &&
					formats.contains("ITF") && formats.contains("EAN_13") &&
					formats.contains("EAN_8") && formats.contains("UPC_E") &&
					formats.contains("INDUSTRIAL_25") && formats.contains("UPC_A")){
				mOned.setChecked(true);
			} else {
				mOned.setChecked(false);
			}*/
		}
		if ((requestCode == REQUEST_ALGORITHM_SETTING && resultCode == RESPONSE_ALGORITHM_SETTING)){
			mSetting = (DBRSetting) data.getSerializableExtra("AlgorithmSetting");
			mImageParameter = mSetting.getImageParameter();
		}
	}
}
