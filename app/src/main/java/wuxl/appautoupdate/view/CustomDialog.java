package wuxl.appautoupdate.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import wuxl.appautoupdate.R;

/**
 * 自定义dialog,确认退出,拨打电话,确认删除等等
 * 
 * @author wuxl
 * 
 */
public class CustomDialog extends Dialog {

	private Context context = null;
	// 布局组件
	private View view = null;
	private TextView dialog_title = null;
	private TextView dialog_info = null;
	private TextView text_ok = null;
	private TextView text_cancel = null;

	// 当前状态

	public CustomDialog(Context context) {
		super(context, R.style.MyDialog);
		this.context = context;
		view = getLayoutInflater().inflate(R.layout.custom_dialog_activity, null);
		setContentView(view);
		// 初始化view
		intiView();
	}

	// 初始化view
	private void intiView() {
		dialog_title = (TextView) view.findViewById(R.id.text_title);
		dialog_info = (TextView) view.findViewById(R.id.text_info);
		text_ok = (TextView) view.findViewById(R.id.call_ok);
		text_cancel = (TextView) view.findViewById(R.id.call_cacel);
	}

	/**
	 * 设置标题
	 * 
	 * @param tilte
	 */
	public void setTitle(String tilte) {
		this.dialog_title.setText(tilte);
	}

	/**
	 * 设置标题
	 */
	public void setTitle(int resId) {
		this.setTitle(context.getString(resId));
	}

	/**
	 * 设置content
	 * 
	 * @param info
	 */
	public void setContent(String info) {
		this.dialog_info.setText(info);
	}

	/**
	 * content
	 * 
	 * @param resId
	 */
	public void setContent(int resId) {
		this.setContent(context.getString(resId));
	}

	/**
	 * 设置确定button
	 * 
	 * @param text
	 * @param listener
	 */
	public void setOKButton(String text, View.OnClickListener listener) {
		this.text_ok.setText(text);
		this.text_ok.setOnClickListener(listener);
	}

	/**
	 * 设置确定button
	 * 
	 * @param resId
	 * @param listener
	 */
	public void setOKButton(int resId, View.OnClickListener listener) {
		this.text_ok.setText(context.getString(resId));
		this.text_ok.setOnClickListener(listener);
	}

	/**
	 * 设置取消button
	 * 
	 * @param listener
	 */
	public void setCancelButton(String text, View.OnClickListener listener) {
		this.text_cancel.setText(text);
		this.text_cancel.setOnClickListener(listener);
	}

	/**
	 * 设置取消button
	 * 
	 * @param listener
	 */
	public void setCancelButton(int resId, View.OnClickListener listener) {
		this.text_cancel.setText(context.getString(resId));
		this.text_cancel.setOnClickListener(listener);
	}

	@Override
	public void show() {
		// 设置dialog
		super.show();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		// 设置dialog
	}

}
