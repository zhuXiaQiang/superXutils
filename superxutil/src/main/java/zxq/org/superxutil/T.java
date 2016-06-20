package zxq.org.superxutil;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Toast统一管理类 
 * @author Zxq
 *
 */
public class T {  
  
    private T()  
    {  
        /* cannot be instantiated */  
        throw new UnsupportedOperationException("cannot be instantiated");  
    }  
  
    public static boolean isDebug = true;
  
    /** 
     * 短时间显示Toast
     *  
     * @param context 
     * @param message 
     */  
    public static void showShort(Context context, CharSequence message)  
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast/Debug时使用
     * @param context
     * @param message
     */
    public static void showShortDebug(Context context, CharSequence message)
    {
        if (isDebug)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

  
    /** 
     * 短时间显示Toast 
     *  
     * @param context 
     * @param message 
     */  
    public static void showShort(Context context, int message)  
    {  
        if (isDebug)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();  
    }  
  
    /** 
     * 长时间显示Toast 
     *  
     * @param context 
     * @param message 
     */  
    public static void showLong(Context context, CharSequence message)  
    {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();  
    }
    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLongDebug(Context context, CharSequence message)
    {
        if (isDebug)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
  
    /** 
     * 长时间显示Toast 
     *  
     * @param context 
     * @param message 
     */  
    public static void showLong(Context context, int message)  
    {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();  
    }  
  
    /** 
     * 自定义显示Toast时间 
     *  
     * @param context 
     * @param message 
     * @param duration 
     */  
    public static void show(Context context, CharSequence message, int duration)  
    {  
        if (isDebug)
            Toast.makeText(context, message, duration).show();  
    }  
  
    /** 
     * 自定义显示Toast时间 
     *  
     * @param context 
     * @param message 
     * @param duration 
     */  
    public static void show(Context context, int message, int duration)  
    {  
        if (isDebug)
            Toast.makeText(context, message, duration).show();  
    }
    
    private static Toast toast;
    
//    private static Toast getToast;
//    private static Toast getToast(Context context,String str){
//        TextView t = new TextView(context);
//        t.setText(str);
//        t.setPadding(16, 8, 16, 8);
//        t.setTextSize(22);
//        t.setTextColor(Color.parseColor("#00ff00"));
//        t.setBackgroundColor(Color.parseColor("#80000000"));
//        if (getToast==null) {
//        	getToast = new Toast(context);
//        	getToast.setDuration(Toast.LENGTH_SHORT);
//		}
//        getToast.setView(t);
//
//        //toast.setView(image);
//        //toast.setView(edit);
//        toast.setGravity(Gravity.CENTER, 0, -60);
//        //toast.show();
//        return getToast;
//    }

    /**
     * 黑科技代替Dialog解決方案,实现友好提示
     * @param context
     * @param str
     */
    public static Toast show(Context context,String str){
        TextView t = new TextView(context);  
        t.setText(str);  
        t.setPadding(16, 8, 16, 8);
        t.setTextSize(18);
        t.setTextColor(Color.parseColor("#00ff00"));
        t.setBackgroundColor(Color.parseColor("#80000000"));
        if (toast==null) {
        	toast = new Toast(context);
        	toast.setDuration(Toast.LENGTH_SHORT);
		}
        toast.setView(t);
        toast.setGravity(Gravity.CENTER, 0, -60);  
        toast.show();
        return toast;
    }
    
    
  
}  
