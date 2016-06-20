package zxq.org.superxutil.net;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import zxq.org.superxutil.event.Event.EventLoadEunm;
import zxq.org.superxutil.ExecutorsUiti;
import zxq.org.superxutil.net.callback.CallBack;
import zxq.org.superxutil.net.callback.CallBackList;
import zxq.org.superxutil.net.callback.CallBackObj;
import zxq.org.superxutil.net.callback.CallBackStr;
import zxq.org.superxutil.net.callback.CallBackUnStr;

/**
 * 网络请求封装
 * @author 朱侠强
 */
public class RunnableUtil {

	private static Gson gson;
	protected static Gson getGson() {
		if (gson == null) {
			synchronized (RunnableUtil.class) {
				if (gson == null) {
					gson = new Gson();
				}
			}
		}
		return gson;
	}

	/**
	 *
	 * @param context
	 * @param completeCallback
	 * @param url
	 * @param clazz
	 * @param cacheTime
     * @param objects
     */
	public static <T> void postRequestData(Context context, final CallBack completeCallback,
									   final String url, final Class<T> clazz, int cacheTime, final Object... objects) {
		RunnableSuperUtil.HttpCallBack callBack = new RunnableSuperUtil.HttpCallBack() {
			@Override
			public void startLoading(Context context) {
				EventLoadEunm event = new EventLoadEunm(HttpErrorEunm.Loading,url);
				event.setParames(objects);
				EventBus.getDefault().post(event);
			}

			@Override
			public void onRequestFailed(Context context, HttpErrorEunm error) {
				EventLoadEunm event = new EventLoadEunm(error,url);
				event.setParames(objects);
				EventBus.getDefault().post(event);
			}
			@Override
			public void onRequestComplete(Context context, String result) {
				if (completeCallback instanceof CallBackUnStr) {
					EventLoadEunm event = new EventLoadEunm(HttpErrorEunm.Complete,url);
					event.setParames(objects);
					EventBus.getDefault().post(event);
					((CallBackUnStr) completeCallback).onRequestUnCompleteStr(result);
					return;
				}
				try {
					JSONObject obj = new JSONObject(result);
					if ("-100".equals(obj.getString("code"))) {
						onRequestFailed(context, HttpErrorEunm.DataNull);
						return;
					}
					EventLoadEunm event = new EventLoadEunm(HttpErrorEunm.Complete,url);
					event.setParames(objects);
					EventBus.getDefault().post(event);
					if (completeCallback instanceof CallBackObj) {
						String dataResult = obj.getJSONArray("result")
								.getString(0);
						((CallBackObj) completeCallback)
								.onRequestCompleteObj(getGson().fromJson(
										dataResult, clazz));
					} else if (completeCallback instanceof CallBackList) {
						JSONArray array = obj.getJSONArray("result");
						List<T> list = new ArrayList<>();
						for (int i = 0; i < array.length(); i++) {
							T t = getGson().fromJson(array.getString(i), clazz);
							list.add(t);
						}

						((CallBackList) completeCallback)
								.onRequestCompleteList(list);
					} else {
						((CallBackStr) completeCallback)
								.onRequestCompleteObj(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
					EventLoadEunm event = new EventLoadEunm(HttpErrorEunm.Unknown,url);
					event.setParames(objects);
					EventBus.getDefault().post(event);
				}
			}
		};
		postRequestData(context, callBack, url, cacheTime, objects);
	}

	/* 构造所有参数 */
	private static void postRequestData(Context context, RunnableSuperUtil.HttpCallBack callBack,
			String url,  int cacheTime, Object... obj) {
		
		ExecutorsUiti.getExecutorService().submit(
				new RunnableSuperUtil.RunnablePostRequest(context, callBack,
						url, cacheTime, obj));
	}

}
