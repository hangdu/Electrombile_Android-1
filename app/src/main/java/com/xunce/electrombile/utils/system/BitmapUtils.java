package com.xunce.electrombile.utils.system;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * <P>
 * Bitmap处理方法
 * <P>
 * 
 * @author Sunny Ding
 * @version 1.00
 */
public class BitmapUtils {
	/**
	 * 改变Bitmap透明度
	 * 
	 * @param sourceImg
	 *            原始Bitmap
	 * @param number
	 *            需要改变的透明度百分比
	 * @return   目标Bitmap
	 * 
	 * */
	public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,
				sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

		number = number * 255 / 100;
		for (int i = 0; i < argb.length; i++) {
			if (argb[i] != 0) {// 透明的颜色不作处理
				argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
			}
		}
		// 用处理好的数组建Bitmap
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(),
                sourceImg.getHeight(), Config.ARGB_8888);
		return sourceImg;
	}
	
	/**
	 * 建立Bitmap防止Out Of Memery Crash
	 * 
	 * @param res
	 *            原始资源
	 * @param resId
	 *            资源id
	 * @param reqWidth 
	 *            目标Bitmap宽度
	 * @param reqHeight 
	 *            目标Bitmap长度  
	 * @return  目标Bitmap
	 * 
	 * */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// BitmapFactory.decodeResource(res, resId, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize =calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		InputStream is =res.openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, options);
	}
	
	//根据指定宽高计算图片适合的尺寸
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * Save Bitmap to a file.保存图片到SD卡。
	 *
	 * @param bitmap
	 * @return error message if the saving is failed. null if the saving is
	 *         successful.
	 * @throws IOException
	 */
	public static void saveBitmapToFile(Bitmap bitmap, String _file)
			throws IOException {
		BufferedOutputStream os = null;
		try {
			File file = new File(_file);
			// String _filePath_file.replace(File.separatorChar +
			// file.getName(), "");
			int end = _file.lastIndexOf(File.separator);
			String _filePath = _file.substring(0, end);
			File filePath = new File(_filePath);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			file.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap compressImageFromFile() {
		String srcPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/crop_result.png";
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;//只读边,不读内容
		BitmapFactory.decodeFile(srcPath, newOpts);

		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//这个地方怎么适配呢   我现在用的直接是px为单位的
		float hh = 426f;
		float ww = 445f;
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置采样率
		newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收

		newOpts.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(srcPath,newOpts);
	}
}
