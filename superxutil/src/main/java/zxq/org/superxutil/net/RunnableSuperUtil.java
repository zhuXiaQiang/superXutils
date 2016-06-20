package zxq.org.superxutil.net;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import zxq.org.superxutil.FileUtils;
import zxq.org.superxutil.FinalUtils;


/**
 * 网络请求封装父类
 * @author 朱侠强
 */
public class RunnableSuperUtil {

	public interface HttpCallBack {
		/**
		 * 开始加载
		 *
		 * @param context
		 */
		void startLoading(Context context);

		/**
		 * (请求成功)
		 */
		void onRequestComplete(Context context, String result);

		/**
		 * 加载失败
		 *
		 * @param context
		 * @param error
		 */
		void onRequestFailed(Context context, HttpErrorEunm error);
	}

	/**
	 * post请求
	 * @author 朱侠强
	 */
	public static class RunnablePostRequest implements Runnable {
		private Context context;
		private String url;
		private HttpCallBack callBack;
		private Object[] obj;
//		private boolean isSetHeader;
		private int cacheTime;

		public RunnablePostRequest(Context context, HttpCallBack callBack,
				String url, int cacheTime, Object... obj) {
			super();
			this.context = context;
			this.callBack = callBack;
			this.url = url;
//			this.isSetHeader = isSetHeader;
			this.obj = obj;
			this.cacheTime = cacheTime;

		}

		@Override
		public void run() {
			// 启用缓存
			if (cacheTime != FinalUtils.TimeUtil.CACHE_NO_CACHE) {
				// 检查本地文件
				String fileName = Math.abs(HttpUtils.getUrl(url, obj)
						.hashCode()) + ".json";
				File file = new File(
						FileUtils.getCachePath(context), fileName);
				if (file.exists()) {
					boolean isOverdue = FileUtils.isOverdue(
							context, file, cacheTime);
					if (isOverdue) {
						// 没有过期加载本地数据
						String result = FileUtils.ReadJson(file);
						if (result != null && result.length() > 0) {
							callBack.onRequestComplete(context, result);
							return;
						}
					}
				}
			}

			// 网络状态不可用
			if (!NetUtils.isConnected(context)) {
				callBack.onRequestFailed(context,
						HttpErrorEunm.NetworkNotAvailable);
				return;
			}
			callBack.startLoading(context);
			
			OkHttpClientManager.Param[] params=new OkHttpClientManager.Param[obj.length/2];
			for (int i = 0; i < params.length; i++) {
				params[i]=new OkHttpClientManager.Param(obj[i].toString(),obj[obj.length / 2 + i].toString());
			}
			
			String result="-2";
			try {
				result= OkHttpClientManager.postAsString(url,context,params);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			String result = HsttpUtils.postHttp(context, url, isSetHeader, obj);
			
			if (cacheTime > 0) {	
				String fileName = Math.abs(HttpUtils.getUrl(url, obj)
						.hashCode()) + ".json";
				isFomat(callBack, context, result, true, fileName);
			} else {
				isFomat(callBack, context, result, false, null);
			}

		}
	}


	/**
	 * 
	 * @Description: TODO(验证数据 合法有效性)
	 * @author 朱侠强
	 * @date 2015-3-3 下午1:43:23
	 * @version V1.0
	 * @param callback
	 * @param result
	 *            true 加载失败 false 加载成功
	 * @return
	 */
	private static boolean isFomat(HttpCallBack callback, Context context,
			String result, boolean isCache, String fileName) {
		// 网络不可用
		if ("-0x404".equals(result)) {
			callback.onRequestFailed(context, HttpErrorEunm.NetworkNotAvailable);
			return false;
		}
		// 加载失败
		if (("-2".equals(result))) {
			callback.onRequestFailed(context, HttpErrorEunm.NetworkError);
			return false;
		}
		callback.onRequestComplete(context, result);
		if (isCache) {
			FileUtils.NewFile(
					new File(FileUtils.getCachePath(context),
							fileName), result);
		}
		return true;
	}

}
