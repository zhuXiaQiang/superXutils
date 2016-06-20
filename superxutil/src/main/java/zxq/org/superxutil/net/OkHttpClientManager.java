package zxq.org.superxutil.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import zxq.org.superxutil.SPUtils;
import zxq.org.superxutil.img.ImageUtils;


public class OkHttpClientManager {

	private static OkHttpClientManager mInstance;
	private OkHttpClient mOkHttpClient;
	private Handler mDelivery;
	private Gson mGson;

	private static final String TAG = "okHttpClinetManager";

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private OkHttpClientManager() {
		mOkHttpClient = new OkHttpClient();
		mOkHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
		mOkHttpClient.setWriteTimeout(15, TimeUnit.SECONDS);
		mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
		mOkHttpClient.setCookieHandler(new CookieManager(null,
				java.net.CookiePolicy.ACCEPT_ORIGINAL_SERVER));
		mDelivery = new Handler(Looper.getMainLooper());
		mGson = new Gson();
	}

	public static OkHttpClientManager getInstance() {
		if (mInstance == null) {
			synchronized (OkHttpClientManager.class) {
				if (mInstance == null) {
					mInstance = new OkHttpClientManager();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 同步的Get请求
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private Response _getAsyn(String url) throws IOException {
		Request request = new Builder().url(url).build();
		Call call = mOkHttpClient.newCall(request);
		
		Response execute = call.execute();
		return execute;
	}

	/**
	 * 同步的Get请求
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private String _getAsString(String url) throws IOException {
		Response execute = _getAsyn(url);
		return execute.body().string();
	}

	/**
	 * 异步的Get请求
	 * 
	 * @param url
	 * @param callback
	 */
	private void _getAsyn(String url, final ResultCallback callback) {
		Request request = new Builder().url(url).build();
		deliveryResult(callback, request);
	}

	/**
	 * 同步的Post请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 */
	private Response _post(String url, Param... params) throws IOException {
		Request request = buildPostRequest(url, params);
		Response response = mOkHttpClient.newCall(request).execute();
		return response;
	}
	
	private Response _post(String url,Context context, Param... params) throws IOException {
		Request request = buildPostRequest(url, context,params);
		Response response = mOkHttpClient.newCall(request).execute();
//		mOkHttpClient.getCookieHandler()
//		response.headers("");
		 List<String> headers = response.headers("set-cookie");
		if (headers.size()>0) {
			String sessionId = headers.get(0).substring(0,
					headers.get(0).indexOf(";"));
			SPUtils.put(context, "Cookie", sessionId);
		}
		return response;
	}

	/**
	 * 同步的Post请求
	 * 
	 * @param url
	 * @param params
	 *            post的参数
	 * @return 字符串
	 */
	private String _postAsString(String url, Param... params) throws IOException {
		Response response = _post(url, params);
		return response.body().string();
	}
	private String _postAsString(String url,Context context, Param... params) throws IOException {
		Response response = _post(url,context, params);
		return response.body().string();
	}
		
	/**
	 * 异步的post请求
	 * @param url
	 * @param callback
	 * @param params
	 */
	private void _postAsyn(String url,final ResultCallback<?> callback,Param...params){
		Request request=buildPostRequest(url, params);
		deliveryResult(callback, request);
	}
	
	/**
	 * 异步的Post请求
	 * @param url
	 * @param callback
	 * @param params
	 */
	private void _postAsyn(String url,final ResultCallback<?> callback,Map<String, String> params){
		Param[] paramsArr=map2Params(params);
		Request request = buildPostRequest(url, paramsArr);
        deliveryResult(callback, request);
	}
	
	/**
	 * 基于Post的文件上传
	 * @param url
	 * @param files
	 * @param fileKeys
	 * @param params
	 * @return
	 * @throws IOException
	 */
	private Response _post(String url,ArrayList<File> files,String[] fileKeys,Param...params) throws IOException{
		Request request=buildMultipartFormRequest(null,url, files, fileKeys, params);
		return mOkHttpClient.newCall(request).execute();
		
	}
	
	/**
	 * 不带参数的单个文件上传
	 * @param url
	 * @param file
	 * @param fileKey
	 * @return
	 * @throws IOException
	 */
	private Response _post(Context context,String url, File file, String fileKey) throws IOException
    {	
		ArrayList<File> files= new ArrayList<File>();
		files.add(file);
		
        Request request = buildMultipartFormRequest(context,url,files, new String[]{fileKey}, null);
        
        return mOkHttpClient.newCall(request).execute();
    }
	/**
	 * 带参数的单个文件上传
	 * @param url
	 * @param file
	 * @param fileKey
	 * @param params
	 * @return
	 * @throws IOException
	 */
//	private Response _post(String url, File file, String fileKey, Param... params) throws IOException
//    {
//        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
//        return mOkHttpClient.newCall(request).execute();
//    }
	
	/**
     * 异步基于post的文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
//    private void _postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException
//    {
//        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
//        deliveryResult(callback, request);
//    }
    
    /**
     * 异步基于post的文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
//    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException
//    {
//        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
//        deliveryResult(callback, request);
//    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
//    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException
//    {
//        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
//        deliveryResult(callback, request);
//    }
    
    /**
     * 异步文件下载
     * @param url
     * @param destFileDir
     * @param callback
     */
    private void _downloadAsyn(final String url,final String destFileDir,final ResultCallback<?> callback){
    	final Request request=new Builder().url(url).build();
    	final Call call=mOkHttpClient.newCall(request);
    	call.enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				// TODO Auto-generated method stub
				sendFailedStringCallback(request, e, callback);
				
			}
			
			@Override
			public void onResponse(Response response) throws IOException {
				InputStream is=null;
				byte[] buf=new byte[2048];
				int len=0;
				FileOutputStream fos=null;
				try {
					is=response.body().byteStream();
					File file=new File(destFileDir,getFileName(url));
					fos=new FileOutputStream(file);
					while ((len=is.read(buf))!=-1) {
						fos.write(buf);
					}
					fos.flush();
					//如果下载成功第一个参数为绝对路径
					sendSuccessResultCallback(file.getAbsolutePath(), callback);
				} catch (Exception e) {
					sendFailedStringCallback(request, e, callback);	
				}finally{
					try
                    {
                        if (is != null) is.close();
                    } catch (IOException e)
                    {
                    }
                    try
                    {
                        if (fos != null) fos.close();
                    } catch (IOException e)
                    {
                    }
				}
			}
			
		});
    }
	
    private String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }
    
    /**
     * 加载图片
     * @param view
     * @param url
     * @param errorResId
     * @throws IOException 
     */
    private Bitmap _displayImage(ImageView view,final String url,final int errorResId) throws IOException{
    	final Request request=new Builder().url(url).build();
    	Call call=mOkHttpClient.newCall(request);
    	Response response = call.execute();

		InputStream is=null;
		
		is=response.body().byteStream();
		ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(is);
        ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(view);
        int inSampleSize=ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
        try {
			is.reset();
		} catch (IOException e) {
			response = _getAsyn(url);
            is = response.body().byteStream();
		}
        BitmapFactory.Options ops=new BitmapFactory.Options();
        ops.inJustDecodeBounds=false;
        ops.inSampleSize=inSampleSize;
        final Bitmap bm=BitmapFactory.decodeStream(is,null,ops);
        return bm;
//		try {
//			
//            
//		} catch (Exception e) {
//			 setErrorResId(view, errorResId);
//		}finally
//        {
//            if (is != null) try
//            {
//                is.close();
//            } catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
    	
//    	call.enqueue(new Callback() {
//    		@Override
//			public void onFailure(Request arg0, IOException arg1) {
//				setErrorResId(view,errorResId);
//			}
//			
//			@Override
//			public void onResponse(Response response) throws IOException {}
//		});
    }
    
    private void setErrorResId(final ImageView view, final int errorResId) {
		mDelivery.post(new Runnable() {
			@Override
			public void run() {
				view.setImageResource(errorResId);
			}
		});
	}
    
    
    /**
     * 同步的Get请求
     * @param url
     * @return Response
     * @throws IOException
     */
    public static Response getAsyn(String url) throws IOException{
    	return getInstance()._getAsyn(url);
    }
    
    /**
     * 同步的Get请求
     * @param url
     * @return String
     * @throws IOException
     */
    public static String getAsString(String url) throws IOException
    {
        return getInstance()._getAsString(url);
    }
    
    /**
     * 异步的Get请求
     * @param url
     * @param callback
     */
    public static void getAsyn(String url, ResultCallback callback)
    {
        getInstance()._getAsyn(url, callback);
    }
    
    /**
     * 同步的Post请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static Response post(String url, Param... params) throws IOException
    {
        return getInstance()._post(url, params);
    }
    
    /**
     * 同步的Post请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String postAsString(String url, Param... params) throws IOException
    {
        return getInstance()._postAsString(url, params);
    }
    public static String postAsString(String url,Context context, Param... params) throws IOException
    {
        return getInstance()._postAsString(url,context, params);
    }
    
    
    /**
     * 异步的Post请求
     * @param url
     * @param callback
     * @param params
     */
    public static void postAsyn(String url, final ResultCallback callback, Param... params)
    {
        getInstance()._postAsyn(url, callback, params);
    }

    /**
     * 异步的Post请求
     * @param url
     * @param callback
     * @param params
     */
    public static void postAsyn(String url, final ResultCallback callback, Map<String, String> params)
    {
        getInstance()._postAsyn(url, callback, params);
    }

    /**
     * Post文件上传
     * @param url
     * @param files
     * @param fileKeys
     * @param params
     * @return
     * @throws IOException
     */
    public static Response post(String url, ArrayList<File> files, String[] fileKeys, Param... params) throws IOException
    {
        return getInstance()._post(url, files, fileKeys, params);
    }
    
    /**
     * 不带参数的单个文件上传
     * @param url
     * @param file
     * @param fileKey
     * @return
     * @throws IOException
     */
    public static Response post(Context context,String url, File file, String fileKey) throws IOException
    {
        return getInstance()._post(context,url, file, fileKey);
    }
    
    /**
     * 带参数的单个文件上传
     * @param url
     * @param file
     * @param fileKey
     * @param params
     * @return
     * @throws IOException
     */
//    public static Response post(Context context,String url, File file, String fileKey, Param... params) throws IOException
//    {
//        return getInstance()._post(context,url, file, fileKey, params);
//    }
    
    /**
     * 异步文件上传
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @param params
     * @throws IOException
     */
//    public static void postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException
//    {
//        getInstance()._postAsyn(url, callback, files, fileKeys, params);
//    }

    /**
     * 异步基于post的文件上传，单文件不带参数上传
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
//    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException
//    {
//        getInstance()._postAsyn(url, callback, file, fileKey);
//    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
//    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException
//    {
//        getInstance()._postAsyn(url, callback, file, fileKey, params);
//    }
    
    /**
     * 加载图片
     * @param view
     * @param url
     * @param errorResId
     * @throws IOException
     */
    public static Bitmap displayImage(ImageView view, String url, int errorResId) throws IOException
    {
        return getInstance()._displayImage(view,url, errorResId);
    }

    /**
     * 加载图片
     * @param view
     * @param url
     */
//    public static void displayImage(final ImageView view, String url)
//    {
//        getInstance()._displayImage(view, url, -1);
//    }
//    
    /**
     * 异步文件下载
     * @param url
     * @param destDir
     * @param callback
     */
    public static void downloadAsyn(String url, String destDir, ResultCallback callback)
    {
        getInstance()._downloadAsyn(url, destDir, callback);
    }
    
	
	
	/**
	 * map转数组
	 * @param params
	 * @return
	 */
	private Param[] map2Params(Map<String, String> params) {
		if (params==null) {
			return new Param[0];
		}
		int size=params.size();
		Param[] res=new Param[size];
		int i=0;
		for (String key:params.keySet()) {
			res[i++]=new Param(key, params.get(key));
		}
		return res;
	}
	
//	private Request buildMultipartFormRequest(String url,File[] files,String[] fileKeys,Param[] params){
//		return buildMultipartFormRequest(null, url, files, fileKeys, params);
//	}
	/**
	 * 构造带参数的图片上传
	 * @param url
	 * @param files
	 * @param fileKeys
	 * @param params
	 * @return
	 */
	private Request buildMultipartFormRequest(Context context,String url,ArrayList<File> files,String[] fileKeys,Param[] params){
		params=validateParam(params);
		MultipartBuilder builder=new MultipartBuilder().type(MultipartBuilder.FORM);
		for (Param param : params) {
			builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null, param.value));
		}
		if (files!=null) {
			RequestBody fileBody=null;
			for (int i  = 0; i < files.size(); i++) {
				File file=files.get(i);
				String fileName=file.getName();
				fileBody=RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
				// 根据文件名设置contentType
				builder.addFormDataPart("file", "file", fileBody);
//                builder.addPart(fileBody);
			}
		}
		
		RequestBody requestBody = builder.build();
		Builder request = new Builder();
//		if (context!=null) {
//			AccessToKenEntity accessEntity = SPUtils.getAccessToKen(context);
//	        request.addHeader("accessToken",
//					accessEntity.getAccessToken());
//	        request.addHeader("areaId",
//					AppUtils.getMetaData(context, "areaId"));
//	        request.addHeader("resourceId",
//					AppUtils.getMetaData(context, "resourceId"));
//	        request.addHeader("unicode",
//					accessEntity.getUnicode());
//	        request.addHeader("usId",
//					accessEntity.getUsid());
//		}
		
        return request
                .url(url)
                .post(requestBody)
                .build();
		
		
	}
	
	private String guessMimeType(String path)
    {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null)
        {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

	/**
	 * 构造PostRequest
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private Request buildPostRequest(String url,Param... params) {
		FormEncodingBuilder builder = new FormEncodingBuilder();
		for (Param param : params) {
			builder.add(param.key, param.value);
		}
		RequestBody requestBody = builder.build();
		return new Builder().url(url).post(requestBody).build();
	}
	private Request buildPostRequest(String url,Context context,Param... params) {
		FormEncodingBuilder builder = new FormEncodingBuilder();
		for (Param param : params) {
			builder.add(param.key, param.value);
		}
		RequestBody requestBody = builder.build();
		
		Builder request = new Builder();
//		if (context!=null) {
//			AccessToKenEntity accessEntity = SPUtils.getAccessToKen(context);
//			request.addHeader(
//					"accessToken",
//					accessEntity.getAccessToken());
//			request.addHeader("areaId",
//					AppUtils.getMetaData(context, "areaId"));
//			request.addHeader("resourceId",
//					AppUtils.getMetaData(context, "resourceId"));
//			request.addHeader("unicode",
//					accessEntity.getUnicode());
//			request.addHeader("usId",
//					accessEntity.getUsid());
//		}
		
		request.addHeader("Cookie",
				SPUtils.get(context, "Cookie", "default").toString());
		return request.url(url).post(requestBody).build();
	}
	

	/**
	 * 发起请求并异步通知
	 * 
	 * @param callback
	 * @param request
	 */
	private void deliveryResult(final ResultCallback callback,
			final Request request) {
		Call call= mOkHttpClient.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Request request, IOException e) {
				sendFailedStringCallback(request, e, callback);
			}

			@Override
			public void onResponse(Response response) {
				try {
					final String string = response.body().string();
					if (callback.mType == String.class) {
						sendSuccessResultCallback(string, callback);
					}else if (callback.mType==List.class) {
						
					} else {
						Object o = mGson.fromJson(string, callback.mType);
						sendSuccessResultCallback(o, callback);
					}
				} catch (JsonParseException e) { // json解析异常
					// TODO: handle exception
					sendFailedStringCallback(response.request(), e, callback);
				} catch (Exception e) {
					sendFailedStringCallback(response.request(), e, callback);
				}
			}

		});
	}

	/**
	 * 异步请求成功通知
	 * 
	 * @param object
	 * @param callback
	 */
	private void sendSuccessResultCallback(final Object object,
			final ResultCallback callback) {
		mDelivery.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.onResponse(object);
				}
			}
		});
	}

	/**
	 * 异步通知请求失败
	 * 
	 * @param request
	 * @param e
	 * @param callback
	 */
	private void sendFailedStringCallback(final Request request,
			final Exception e, final ResultCallback callback) {
		mDelivery.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.onError(request, e);
				}
			}
		});
	}

	public static abstract class ResultCallback<T> {
		Type mType;

		public ResultCallback() {
			mType=getSuperclassTypeParameter(getClass());
		}

		static Type getSuperclassTypeParameter(Class<?> subclass) {
			Type superclass = subclass.getGenericSuperclass();
			if (superclass instanceof Class) {
				throw new RuntimeException("Missing type parameter.");
			}

			ParameterizedType parameterized = (ParameterizedType) superclass;
			return $Gson$Types.canonicalize(parameterized
					.getActualTypeArguments()[0]);
		}

		public abstract void onError(Request request, Exception e);

		public abstract void onResponse(T response);

	}

	
	private Param[] validateParam(Param[] params)
    {
        if (params == null)
            return new Param[0];
        else return params;
    }
	public static class Param {
		public Param() {
		}

		public Param(String key, String value) {
			this.key = key;
			this.value = value;
		}

		String key;
		String value;
	}

	
	
	private static final String SESSION_KEY = "Set-Cookie";
    private static final String mSessionKey = "JSESSIONID";

    private Map<String, String> mSessions = new HashMap<String, String>();
	
	
    
	
//	public static void ss() {
//		// 创建一个okHttpClient 对象
//		OkHttpClient mOkHttpClient = new OkHttpClient();
//		// RequestBody requestBody=new ;
//		// 创建一个Request
//		// Builder builder = new Request.Builder();
//		FormEncodingBuilder builder = new FormEncodingBuilder();
//		builder.add("username", "朱侠强");
//
//		Request request = new Request.Builder().url("").post(builder.build())
//				.build();
//
//		// new call
//		Call call = mOkHttpClient.newCall(request);
//		call.enqueue(new Callback() {
//
//			@Override
//			public void onResponse(Response response) throws IOException {
//				// TODO Auto-generated method stub
//				String ss = response.body().string();
//				System.out.println(ss);
//
//			}
//
//			@Override
//			public void onFailure(Request arg0, IOException arg1) {
//				// TODO Auto-generated method stub
//				System.out.println(arg0);
//				System.out.println();
//			}
//		});
//	}
	

}
