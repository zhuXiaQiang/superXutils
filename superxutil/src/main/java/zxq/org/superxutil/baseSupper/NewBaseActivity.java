package zxq.org.superxutil.baseSupper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import zxq.org.superxutil.R;
import zxq.org.superxutil.T;
import zxq.org.superxutil.event.Event;
import zxq.org.superxutil.net.HttpErrorEunm;

/**
 * 新的父类
 *
 * @author 朱侠强
 */
public class NewBaseActivity extends Activity {
	private boolean isShow;

	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
		setContentView(R.layout.activity_base_all);
		initActionBarBase();

	}


	public void setSuperView(@LayoutRes int layoutResID) {
		frame.addView(getLayoutInflater().inflate(layoutResID, null));
	}

	public void setSuperView(View view) {
		frame.addView(view);
	}


	public View actionTitle;
	public LinearLayout actionLeftLayout;
	public TextView actionTitleText;
	public LinearLayout actionRightLayout;
	public LinearLayout layoutLoad;
	private void initActionBarBase(){
		initActionBarBase(R.layout.actionbar_img_text_img);
	}

	public void initActionBarBase(int resId) {
		View v = getLayoutInflater().inflate(resId,null);
		FrameLayout actionbar= (FrameLayout) findViewById(R.id.actionbar);
		actionbar.removeAllViews();
		actionbar.addView(v);
		actionTitle = findViewById(R.id.actionbar_title);
		frame = (FrameLayout) findViewById(R.id.frame);
		if (actionTitle == null) {
			return;
		}
		actionLeftLayout = (LinearLayout) actionTitle
				.findViewById(R.id.action_leftLayout);
		actionLeftLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KeyBack();
				finish();
			}
		});
		actionRightLayout = (LinearLayout) actionTitle
				.findViewById(R.id.action_rightLayout);
		if (actionRightLayout != null) {
			actionRightLayout.setVisibility(View.GONE);
		}

		actionTitleText = (TextView) actionTitle
				.findViewById(R.id.action_title_text);

		layoutLoad = (LinearLayout) actionTitle.findViewById(R.id.layoutLoad);

	}

	public void KeyBack() {
	}


	public Toast ts;

	public void onEventMainThread(Event.EventLoadEunm event) {
		if (layoutLoad != null) {
			if (event.getHttpErrorEunm() == HttpErrorEunm.Loading) {
				layoutLoad.setVisibility(View.VISIBLE);
			} else {
				layoutLoad.setVisibility(View.GONE);
			}
		}
		if (isShow) {
			if (event.getHttpErrorEunm() != HttpErrorEunm.Complete && event.getHttpErrorEunm() != HttpErrorEunm.Loading) {
				ts = T.show(getContext(), event.getHttpErrorEunm().getErrorResult());
				ts.show();
			}
		}

		if (!getEuals(event)) {
			return;
		}
		if (event.getHttpErrorEunm() != HttpErrorEunm.Loading
				&& HttpErrorEunm.Complete != event.getHttpErrorEunm()) {
			if (event.getHttpErrorEunm() != HttpErrorEunm.DataNull) {
				if (frame != null) {
					if (frame.getChildCount() > 1) {
						frame.removeView(errorView);
					}
					errorView = getLayoutInflater().inflate(
							R.layout.error_nonet_loaderror, null);
					TextView text = (TextView) errorView.findViewById(R.id.text);
					text.setText(HttpErrorEunm.NetworkNotAvailable.getErrorResult());

					frame.addView(errorView);
					setErrorListener(errorView);
				}
			} else if (event.getHttpErrorEunm() == HttpErrorEunm.DataNull) {
				if (frame != null) {
					if (frame.getChildCount() > 1) {
						frame.removeView(errorView);
					}
					errorView = getLayoutInflater().inflate(
							R.layout.error_datanull, null);
					TextView text = (TextView) errorView.findViewById(R.id.text);
					text.setText(event.getHttpErrorEunm().getErrorResult());
					frame.addView(errorView);
					setErrorDataNull();
				}
			}
		} else if (HttpErrorEunm.Complete == event.getHttpErrorEunm()) {
			frame.removeView(errorView);
		}

	}

	public void setErrorListener(View view) {

	}

	public void setErrorDataNull() {
	}

	public FrameLayout frame;
	public View errorView;

	//private Button btnError;
	public boolean getEuals(Event.EventLoadEunm event) {
		return false;
	}

	public Context getContext() {
		return this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (ts != null) {
			ts.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this);
		}
	}

	@Override
	public void onStart() {
		super.onStart();


	}

	@Override
	public void onStop() {
		super.onStop();


	}
}
