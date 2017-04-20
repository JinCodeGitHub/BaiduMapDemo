package com.yushan.baidumapdemo;

import android.content.Context;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author meric
 * @version 创建时间：2015-1-12 下午1:44:01
 * 类说明
 */
public class ToastUtil {
	/**
	 * 屏幕中央
	 * @param context
	 * @param message
	 */
	private static Toast t;

	public static void showCenter(Context context, String message){
		if(t == null){
			t = Toast.makeText(context, message,
					Toast.LENGTH_SHORT);
		}
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
		t = null;
	}
	/**
	 * 屏幕下方
	 * @param context
	 * @param message
	 */
	public static void showBottom(Context context, String message){
		if(t == null){
			t = Toast.makeText(context, message,
					Toast.LENGTH_SHORT);
		}
		t.setGravity(Gravity.BOTTOM, 0, 170);
		t.show();
		t = null;
	}
	/**
	 *
	 * @param context
	 * 			上下文
	 * @param resId
	 * 			资源id
	 */
	public static void showCenter(Context context, int resId){
		if (t==null) {
			t = Toast.makeText(context, context.getResources().getString(resId),
					Toast.LENGTH_SHORT);
		}
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
		t = null;
	}


	/**
	 * 添加方法（Toast字体颜色变更）
	 * @param context
	 * @param message
     */
	public static void showToast(Context context, Spanned message){

		View layout = LayoutInflater.from(context).inflate(R.layout.layout_toast,null);
		TextView textView = (TextView) layout.findViewById(R.id.tv_info);


		Toast toast = new Toast(context);
		toast.setGravity( Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		textView.setText(message);
		toast.setView(layout);
		toast.show();

	}

	public static void showToast(Context context, String message){

		View layout = LayoutInflater.from(context).inflate(R.layout.layout_toast,null);
		TextView textView = (TextView) layout.findViewById(R.id.tv_info);


		Toast toast = new Toast(context);
		toast.setGravity( Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		textView.setText(message);
		toast.setView(layout);
		toast.show();

	}


}
