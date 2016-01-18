package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.covics.zxingscanner.OnDecodeCompletionListener;
import com.covics.zxingscanner.ScannerView;
import com.xunce.electrombile.R;

/**
 * 二维码扫描页面
 * 
 */	
public class ScannerActivity extends Activity implements
		OnDecodeCompletionListener {
	private ScannerView scannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scaner);
		initView();
	}

	private void initView() {
		scannerView = (ScannerView) findViewById(R.id.scanner_view);
		scannerView.setOnDecodeListener(this);
	}

	@Override
	public void onDecodeCompletion(String barcodeFormat, String barcode,
			Bitmap bitmap) {
		if (barcode != null) {
			Intent intent = new Intent();
			intent.putExtra("result", barcode);
			setResult(0x02, intent);
			this.finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		scannerView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		scannerView.onPause();
	}
}
