package zxq.org.superxutil.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import zxq.org.superxutil.ExecutorsUiti;
import zxq.org.superxutil.FileUtils;
import zxq.org.superxutil.net.OkHttpClientManager;

/**
 * 图片加载
 * @author 
 */
public class ImageLoader {
	/**
	 * 图片缓存的核心类
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 线程池
	 */
	//private ExecutorService mThreadPool;
	/**
	 * 线程池的线程数量，默认为1
	 */
	private int mThreadCount = 1;
	/**
	 * 队列的调度方式
	 */
	private Type mType = Type.LIFO;
	/**
	 * 任务队列
	 */
	private LinkedList<Runnable> mTasks;
	/**
	 * 轮询的线程
	 */
	private Thread mPoolThread;
	private Handler mPoolThreadHander;

	/**
	 * 运行在UI线程的handler，用于给ImageView设置图片
	 */
	private Handler mHandler;

	/**
	 * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
	 */
	private volatile Semaphore mSemaphore = new Semaphore(0);

	/**
	 * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
	 */
	private volatile Semaphore mPoolSemaphore;

	private static ImageLoader mInstance;

	private Context context;

	/**
	 * 队列的调度方式
	 * 
	 * @author zhy
	 * 
	 */
	public enum Type {
		FIFO, LIFO
	}

	/**
	 * 单例获得该实例对象
	 * 
	 * @return
	 */
	public static ImageLoader getInstance(Context context) {

		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(context, Type.LIFO);

				}
			}
		}
		return mInstance;
	}

	private ImageLoader(Context context, Type type) {
		init(type);
		this.context = context;
	}

	private void init(Type type) {
		// loop thread
		mPoolThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				mPoolThreadHander = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						ExecutorsUiti.getExecutorServiceImg().execute(getTask());
						try {
							mPoolSemaphore.acquire();
						} catch (InterruptedException e) {
						}
					}
				};
				// 释放一个信号量
				mSemaphore.release();
				Looper.loop();
			}
		};
		mPoolThread.start();

		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			};
		};
		//mThreadPool = ;
		mPoolSemaphore = new Semaphore(mThreadCount);
		mTasks = new LinkedList<Runnable>();
		mType = type == null ? Type.LIFO : type;

	}
	
	public interface LoadImgCallback{
		public void loadError(ImageView img);
	}
	public void loadImage(final boolean isFile, final String path,
			final ImageView imageView,final LoadImgCallback callBack) {

		if (path==null) {
			return;
		}
		// set tag
		imageView.setTag(path);
		// UI线程
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					ImageView imageView = holder.imageView;
					Bitmap bm = holder.bitmap;
					String path = holder.path;
					if (imageView.getTag().toString().equals(path)) {
						if (bm!=null) {
							imageView.setImageBitmap(bm);
						}else {
							if (callBack!=null) {
								callBack.loadError(imageView);
							}
						}
						
					}
				}
			};
		}
		Bitmap bm = getBitmapFromLruCache(path);
		if (bm != null) {
			ImgBeanHolder holder = new ImgBeanHolder();
			holder.bitmap = bm;
			holder.imageView = imageView;
			holder.path = path;
			Message message = Message.obtain();
			message.obj = holder;
			mHandler.sendMessage(message);
		} else {
			addTask(new Runnable() {
				@Override
				public void run() {
					ImageSize imageSize = getImageViewWidth(imageView);

					int reqWidth = imageSize.width;
					int reqHeight = imageSize.height;

					Bitmap bm = decodeSampledBitmapFromResource(isFile, path,imageView,
							reqWidth, reqHeight);
					
					addBitmapToLruCache(path, bm);
					ImgBeanHolder holder = new ImgBeanHolder();
					holder.bitmap = getBitmapFromLruCache(path);
					holder.imageView = imageView;
					holder.path = path;
					Message message = Message.obtain();
					message.obj = holder;
					mHandler.sendMessage(message);
					mPoolSemaphore.release();
				}
			});
		}
	
	}
	
	public void loadImageNoLru(final String path,
			final ImageView imageView) {

		if (path==null) {
			return;
		}
		// set tag
		imageView.setTag(path);
		// UI线程
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					ImageView imageView = holder.imageView;
					Bitmap bm = holder.bitmap;
					String path = holder.path;
					if (imageView.getTag().toString().equals(path)) {
						if (bm!=null) {
							imageView.setImageBitmap(bm);
						}
						
					}
				}
			};
		}
		addTask(new Runnable() {
			@Override
			public void run() {
				ImageSize imageSize = getImageViewWidth(imageView);

				int reqWidth = imageSize.width;
				int reqHeight = imageSize.height;

				Bitmap bm = decodeSampledBitmapFromResource(false, path,
						imageView,reqWidth, reqHeight);
//				addBitmapToLruCache(path, bm);
//				addBitmapToLruCache(path, bm);
				ImgBeanHolder holder = new ImgBeanHolder();
				holder.bitmap = bm;
				holder.imageView = imageView;
				holder.path = path;
				Message message = Message.obtain();
				message.obj = holder;
				mHandler.sendMessage(message);
				mPoolSemaphore.release();
			}
		});
	}
	
	/**
	 * 加载图片
	 * 
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final boolean isFile, final String path,
			final ImageView imageView) {
		loadImage(isFile, path, imageView,null);
	}

	/**
	 * 添加一个任务
	 * 
	 * @param runnable
	 */
	private synchronized void addTask(Runnable runnable) {
		try {
			// 请求信号量，防止mPoolThreadHander为null
			if (mPoolThreadHander == null)
				mSemaphore.acquire();
		} catch (InterruptedException e) {
		}
		mTasks.add(runnable);

		mPoolThreadHander.sendEmptyMessage(0x110);
	}

	/**
	 * 取出一个任务
	 * 
	 * @return
	 */
	private synchronized Runnable getTask() {
		if (mType == Type.FIFO) {
			return mTasks.removeFirst();
		} else if (mType == Type.LIFO) {
			return mTasks.removeLast();
		}
		return null;
	}

	/**
	 * 单例获得该实例对象
	 * 
	 * @return
	 */
	public static ImageLoader getInstance(Context context, Type type) {

		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(context, type);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 根据ImageView获得适当的压缩的宽和高
	 * 
	 * @param imageView
	 * @return
	 */
	private ImageSize getImageViewWidth(ImageView imageView) {
		ImageSize imageSize = new ImageSize();
		final DisplayMetrics displayMetrics = imageView.getContext()
				.getResources().getDisplayMetrics();
		final LayoutParams params = imageView.getLayoutParams();

		int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView
				.getWidth(); // Get actual image width
		if (width <= 0)
			width = params.width; // Get layout width parameter
		if (width <= 0)
			width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
																	// maxWidth
																	// parameter
		if (width <= 0)
			width = displayMetrics.widthPixels;
		int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView
				.getHeight(); // Get actual image height
		if (height <= 0)
			height = params.height; // Get layout height parameter
		if (height <= 0)
			height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
																		// maxHeight
																		// parameter
		if (height <= 0)
			height = displayMetrics.heightPixels;
		imageSize.width = width;
		imageSize.height = height;
		return imageSize;

	}

	/**
	 * 从LruCache中获取一张图片，如果不存在就返回null。
	 */
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}

	/**
	 * 往LruCache中添加一张图片
	 * 
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToLruCache(String key, Bitmap bitmap) {
		if (getBitmapFromLruCache(key) == null) {
			if (bitmap != null)
				mLruCache.put(key, bitmap);
		}
	}

	/**
	 * 计算inSampleSize，用于压缩图片
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的宽度
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;

		if (width > reqWidth && height > reqHeight) {
			// 计算出实际宽度和目标宽度的比率
			int widthRatio = Math.round((float) width / (float) reqWidth);
			int heightRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = Math.max(widthRatio, heightRatio);
		}
		return inSampleSize;
	}

	/**
	 * 根据计算的inSampleSize，得到压缩后图片
	 * @param isFile
	 * @param imageUrl
	 * @param imageView
	 * @param reqWidth
	 * @param reqHeight
     * @return
     */
	private Bitmap decodeSampledBitmapFromResource(boolean isFile,
			String imageUrl,ImageView imageView, int reqWidth, int reqHeight) {
		if (!isFile) {
			if (!(imageUrl!=null&&imageUrl.length()>0&&imageUrl.indexOf("http://")!=-1)) {
				return null;
			}
		}
		
		// 判断本地是否存在
		File file = null;
		if (isFile) {
			file = new File(imageUrl);
		} else {
//			file = new File(Environment.getExternalStorageDirectory().getPath()
//					+ context.getCacheDir(), "" + Math.abs(imageUrl.hashCode()));
			file = new File(FileUtils.getImgCachePath(""+Math.abs(imageUrl.hashCode())));
//			file = new File(Environment.getExternalStorageDirectory().getPath()
//					+ context.getCacheDir(), "" + Math.abs(imageUrl.hashCode())+".png");
		}

		Bitmap bitmap = null;
		// 如果存在 直接从本地读取
		if (file.exists()) {
			// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			// 调用上面定义的方法计算inSampleSize值
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);
			// 使用获取到的inSampleSize值再次解析图片
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			if (bitmap != null) {
				return compressBitmap(bitmap);
			}
		}

		if (isFile) {
			return bitmap;
		}
		
//		bitmap = HttpUtils.getBitmap(context,imageUrl);
		try {
			bitmap= OkHttpClientManager.displayImage(imageView,imageUrl,-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bitmap != null) {
			compressBitmap(context,file, bitmap);
		} else {
			// 重新请求一次
//			bitmap = HttpUtils.getBitmap(context,imageUrl);
			try {
				bitmap=OkHttpClientManager.displayImage(imageView,imageUrl,-1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (bitmap != null) {
				compressBitmap(context,file, bitmap);
			}
		}
		if (bitmap != null) {
			return compressBitmap(bitmap);
		}
		return bitmap;
	}

	/**
	 * 
	 * @param bitmap
	 * @return
	 */
	private Bitmap compressBitmap(Bitmap bitmap) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		/*int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}*/
		bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmaps = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmaps;
	}

	/**
	 * 压缩保存
	 * @param file
	 * @param bitmap
	 */
	public static void compressBitmap(Context context,File file, Bitmap bitmap) {
		try {
			// 判断目录是否存在
			if (!new File(Environment.getExternalStorageDirectory().getPath()
					+ context.getCacheDir()).isDirectory()) {
				// 创建目录
				file.mkdirs();
			}
			// 判断文件是否存在如果存在就删除
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private class ImgBeanHolder {
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}

	private class ImageSize {
		int width;
		int height;
	}

	/**
	 * 反射获得ImageView设置的最大宽度和高度
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;

				Log.e("TAG", value + "");
			}
		} catch (Exception e) {
		}
		return value;
	}

}
