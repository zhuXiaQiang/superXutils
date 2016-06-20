package zxq.org.superxutil;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 格式化工具相关
 * @author Zxq
 * 
 */
public class FormatUtils {
	
	public  abstract class FriendlyTimes{
		public  abstract String getFriendlyTime();
	}
	
	
	public static class REPattern{
		// 判断手机格式是否正确
		public static boolean isPhoneNumber(String mobiles) {
			Pattern p = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		}
		public static boolean isTelePhone(String mobiles) {
			Pattern p = Pattern.compile("^(0[0-9]{2,3}\\-)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		}
	}

	@SuppressLint("SimpleDateFormat")
	public final static class DateFormat {
		// 对于创建SimpleDateFormat传入的参数：EEEE代表星期，如“星期四”；MMMM代表中文月份，如“十一月”；MM代表月份，如“11”；yyyy代表年份，如“2010”；dd代表天，如“25”
		
		public static final String format_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
		public static final String format_yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
		public static final String format_MMddHHmm = "MM-dd HH:mm";
		public static final String format_yyyyMMdd = "yyyy-MM-dd";
		public static final String format_HHmm = "HH:mm";
		public static final String format_MMmonth_ddDay = "MM月dd日";
		public static final String HHmmss000 = "00:00:00";
		public static final String format_MMdd_EEEE_AMPM = "MM-dd EEEE";
		/**
		 * 获取指定格式的当前时间
		 * @param fmtStr
		 * @return
		 */
		public static String formatDateToStr(String fmtStr) {
			SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
			return fmt.format(new Date());
		}
		/**
		 * 将字符串转换成Date
		 * @param formatStr 字符串格式
		 * @param str  转化的字符串
		 * @return
		 */
		public static Date formatStrToDate(String formatStr, String str) {
			try {
				SimpleDateFormat fmt = new SimpleDateFormat(formatStr);
				return fmt.parse(str);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return new Date();
		}

		/**
		 * 把日期转换成字符串
		 * 
		 * @param formatStr
		 * @param date
		 * @return
		 */
		public static String formatDateToStr(String formatStr, Date date) {
			SimpleDateFormat fmt = new SimpleDateFormat(formatStr);
			return fmt.format(date);
		}
		/**
		 * 指定格式的日期字符串转换为MM-dd EEEE AMPM
		 * 
		 * @param fmtStrLift
		 *            当前字符串格式
		 * @param str
		 *            需要转换的字符串
		 * @return
		 */
		public static String formatStrToStr(String fmtStrLift, String str) {
			try {
				Date date = formatStrToDate(fmtStrLift, str);
				Calendar cl = Calendar.getInstance();
				cl.setTime(date);
				int i = cl.get(GregorianCalendar.AM_PM);
				String AmPm = "";
				if (i == 0) {
					AmPm = "上午";
				} else {
					AmPm = "下午";
				}
				return formatDateToStr(format_MMdd_EEEE_AMPM, cl.getTime()) + " " + AmPm;
			} catch (Exception e) {

			}
			return str;
		}
		
		/**
		 * 将指定格式的字符串转换为指定格式
		 * @param fmtStrLift
		 * @param str
		 * @param fmtStrFilt
		 * @return
		 */
		public static String formatStrToStr(String fmtStrLift, String str,
				String fmtStrFilt) {
			try {
				Date date = formatStrToDate(fmtStrLift, str);
				return formatDateToStr(fmtStrFilt, date);
			} catch (Exception e) {
			}
			return str;
		}
		
		/**
		 * 获取友好时间
		 * @param date
		 * @return
		 */
		public static String getFriendlyTime(Date date){
			//当前时间
			String curDateStr=formatDateToStr(format_yyyyMMdd);
			//传进来的时间
			String paramDateStr=formatDateToStr(format_yyyyMMdd, date);
			
			String paramYYYY=formatDateToStr("yyyy", date);
			String curYYYY=formatDateToStr("yyyy");
			
//			String paramMM=formatDateToStr("MM", date);
//			String curMM=formatDateToStr("MM");
			
			
			//判断是否是同一天
			if (curDateStr.equals(paramDateStr)) {
				return formatDateToStr("HH:mm", date);
			}else if (paramYYYY.equals(curYYYY)) { //判断是否是同一年
				return formatDateToStr("MM-dd HH:mm", date);
				//				if (paramMM.equals(curMM)) { //判断是否同一个月
//					return formatDateToStr("dd号 HH:mm", date);
//				}else {
//					return formatDateToStr("MM-dd HH:mm", date);
//				}
			}else {
				return formatDateToStr("yyyy-MM-dd HH:mm", date);
			}
		}
		
		public static String getFriendlyTime(String fmtStr,String timeStr){
			//当前时间
			String curDateStr=formatDateToStr(format_yyyyMMdd);
			Calendar curDate=Calendar.getInstance();
			
			//传进来的时间
			String paramDateStr=formatStrToStr(fmtStr, timeStr, format_yyyyMMdd);
			Date tempDate= formatStrToDate(fmtStr, timeStr);
			Calendar paramDate=Calendar.getInstance();
			
			paramDate.setTime(tempDate);
			//时分
			String hourMinute=formatDateToStr("HH", paramDate.getTime())+":"+formatDateToStr("mm", paramDate.getTime());
			//判断是否是同一天
			if (curDateStr.equals(paramDateStr)) {
				return hourMinute;
			}else if (curDate.get(Calendar.MONTH )==paramDate.get(Calendar.MONTH)) {//判断是不是同一个月
				if (curDate.get(Calendar.DATE)-paramDate.get(Calendar.DATE)==1) {//判断是不是昨天
					return "昨天";
				}else {
					return (paramDate.get(Calendar.MONTH)+1)+"月"+paramDate.get(Calendar.DATE)+"日";
				}
			}else {
				return (paramDate.get(Calendar.MONTH)+1)+"月"+paramDate.get(Calendar.DATE)+"日";
			}
		}
		
		
	}

}
