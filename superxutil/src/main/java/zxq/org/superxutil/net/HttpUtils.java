package zxq.org.superxutil.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 访问网络相关类
 * @author 朱侠强
 */

public class HttpUtils {

	/**
	 * 把InputStream 转化成 String
	 * @param inSream
	 * @param charsetName
	 * @return
	 * @throws Exception
	 */
	public static String readData(InputStream inSream, String charsetName)
			throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inSream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inSream.close();
		return new String(data, charsetName);
	}

	/**
	 * 通过URL 地址 得到JSONArray
	 * @param urlStr
	 * @return
	 */
	public static String getJSONArray(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			InputStream inputStream = connection.getInputStream();
			String neirong = HttpUtils.readData(inputStream, "utf-8");

			return neirong;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}


	public static Bitmap stringtoBitmap(String string) {
		// 将字符串转换成Bitmap类型
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
			bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;

	}

	/**
	 * 將HttpEntity 對象装换成String
	 * @param inputReader
	 * @return
	 */
	public static String getStringFromHttp(InputStreamReader inputReader) {
		StringBuffer buffer = new StringBuffer();
		// 获取输入流
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(inputReader);
			// 将返回的数据读到buffer中
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				buffer.append(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				if (null != reader) {
					reader.close();
					reader = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}



	/**
	 * 拼接URL 参数
	 * @param url
	 * @param nameValue
	 * @return
	 */
	public static String getUrl(String url, Object... nameValue) {
		url += "?";

		for (int i = 0; i < nameValue.length / 2; i++) {
			url += nameValue[i].toString() + "="
					+ nameValue[nameValue.length / 2 + i].toString();
			if (i < nameValue.length / 2 - 1) {
				url += "&";
			}
		}

		return url;
	}

}
