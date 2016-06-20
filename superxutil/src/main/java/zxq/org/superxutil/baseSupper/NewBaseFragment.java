package zxq.org.superxutil.baseSupper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import zxq.org.superxutil.R;
import zxq.org.superxutil.event.Event.EventLoadEunm;
import zxq.org.superxutil.net.HttpErrorEunm;

/**
 * 新的父类
 * @author 朱侠强
 */
public class NewBaseFragment extends Fragment {
	public View rootView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
	}
	

	public Context getContext() {
		return getActivity();
	}
	
	public void onEventMainThread(EventLoadEunm event){
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
					errorView =((Activity) getContext()).getLayoutInflater().inflate(
							R.layout.error_nonet_loaderror, null);
					TextView text = (TextView) errorView.findViewById(R.id.text);
					text.setText(HttpErrorEunm.NetworkNotAvailable.getErrorResult());
					
					frame.addView(errorView);
					setErrorListener(errorView);
				}
			} else if (event.getHttpErrorEunm() == HttpErrorEunm.DataNull) {
				if (frame!=null) {
					if (frame.getChildCount()>1) {
						frame.removeView(errorView);
					}
					errorView = ((Activity) getContext()).getLayoutInflater().inflate(
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
