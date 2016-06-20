package zxq.org.superxutil.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import zxq.org.superxutil.img.ImageLoader;


public class ViewHolder
{
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;
	private Context context;
	//private AsyncImageLoader asyncImageLoader;
	

	private ViewHolder(Context context, ViewGroup parent, int layoutId,
			int position)
	{
		this.mPosition = position;
		this.context=context;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);
		// setTag
		mConvertView.setTag(this);
		//asyncImageLoader=new AsyncImageLoader();
	}



	/**
	 * 拿到一个ViewHolder对象
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static ViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position)
	{
		if (convertView == null)
		{
			return new ViewHolder(context, parent, layoutId, position);
		}
		return (ViewHolder) convertView.getTag();
	}

	public View getConvertView()
	{
		return mConvertView;
	}

	/**
	 * 通过控件的Id获取对于的控件，如果没有则加入views
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId)
	{
		View view = mViews.get(viewId);
		if (view == null)
		{
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, String text)
	{
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int drawableId)
	{
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);

		return this;
	}

	/**
	 * 为ImageView设置图片
	 * @param viewId
	 * @param bm
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bm)
	{
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * @param viewId
	 * @param url
	 * @return
	 */
	public ViewHolder setImageByUrl(int viewId, String url)
	{
		
		/*asyncImageLoader.loadDrawable(url, 
				(ImageView) getView(viewId));*/
		
		ImageLoader.getInstance(context).loadImage(false,url,
		(ImageView) getView(viewId));
		return this;
	}
//	public ViewHolder setRoundImageViewByXfermodeByUrl(int viewId,LoadImgCallback callBack, String url)
//	{
//		
//		ImageLoader.getInstance(context).loadImage(false,url,
//			(RoundImageViewByXfermode) getView(viewId),callBack);
//		return this;
//	}
	public ViewHolder setImageByUrl(int viewId, ImageLoader.LoadImgCallback callBack, String url)
	{
		
		ImageLoader.getInstance(context).loadImage(false,url,
			(ImageView) getView(viewId),callBack);
		return this;
	}
	public ViewHolder setImageByUrl(int viewId,boolean isFile, String url)
	{
		
		ImageLoader.getInstance(context).loadImage(isFile,url,
			(ImageView) getView(viewId));
		return this;
	}

	public int getPosition()
	{
		return mPosition;
	}

}
