package zxq.org.superxutil.baseSupper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import zxq.org.superxutil.R;
import zxq.org.superxutil.T;
import zxq.org.superxutil.event.Event.EventLoadEunm;
import zxq.org.superxutil.net.HttpErrorEunm;

/**
 * 新的父类
 * @author 朱侠强
 */
public class NewBaseFragmentActivity extends FragmentActivity {
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
	}
	
	public View actionTitle;
	public LinearLayout actionLeftLayout;
	public TextView actionTitleText;
	public LinearLayout actionRightLayout;
	public LinearLayout layoutLoad;

	public void initActionBarBase() {
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
				finish();
			}
		});
		actionRightLayout = (LinearLayout) actionTitle
				.findViewById(R.id.action_rightLayout);
		if (actionRightLayout!=null) {
			actionRightLayout.setVisibility(View.GONE);
		}

		actionTitleText = (TextView) actionTitle
				.findViewById(R.id.action_title_text);

		layoutLoad = (LinearLayout) actionTitle.findViewById(R.id.layoutLoad);

	}

	public Toast ts;

	public  void onEventMainThread(EventLoadEunm event) {
		if (layoutLoad!=null) {
			if (event.getHttpErrorEunm() == HttpErrorEunm.Loading) {
				layoutLoad.setVisibility(View.VISIBLE);
			} else {
				layoutLoad.setVisibility(View.GONE);
			}
		}
		if (isShow) {
			if (event.getHttpErrorEunm() != HttpErrorEunm.Complete&&event.getHttpErrorEunm() != HttpErrorEunm.Loading) {
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
				if (frame!=null) {
					if (frame.getChildCount()>1) {
						frame.removeView(errorView);
					}
					errorView = getLayoutInflater().inflate(
							R.layout.error_nonet_loaderror, null);
					TextView text = (TextView) errorView.findViewById(R.id.text);
					text.setText(HttpErrorEunm.NetworkNotAvailable.getErrorResult());
					
					frame.addView(errorView);
					setErrorListener(frame);
				}
			} else if (event.getHttpErrorEunm() == HttpErrorEunm.DataNull) {
				if (frame!=null) {
					if (frame.getChildCount()>1) {
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
	public void setErrorListener(View btnError) {
			
	}
	public void setErrorDataNull(){}

	public FrameLayout frame;
	public View errorView;
	//private Button btnError;
	public  boolean getEuals(EventLoadEunm event){
		return false;
	}
	
	public Context getContext() {
		return this;
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (ts!=null) {
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
}
