package zxq.org.superxutil;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import zxq.org.superxutil.img.ImageLoader;
import zxq.org.superxutil.net.HttpUtils;
import zxq.org.superxutil.net.NetUtils;


/**
 * 文件处理工具类
 * @author 朱侠强
 */
public class FileUtils {
	
	public static final String FINDL_AUDIO="audio";
	public static final String FINDL_IMG="img";
	/**
	 * 缓存目录
	 * 
	 * @param context
	 * @return
	 */
	public static final File getCachePath(Context context) {
		String path = Environment.getExternalStorageDirectory().getPath()
				+ context.getCacheDir();
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return file;
	}
	private static final String getCachePath(String filePath) {
		String path = Environment.getExternalStorageDirectory().getPath()+"/yhb/"+ filePath;
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return file.toString();
	}


	public static final String getAudioCachePath(String fileName) {
		return getCachePath(FINDL_AUDIO)+"/"+fileName;
	}
	public static final String getImgCachePath(String fileName) {
		return getCachePath(FINDL_IMG)+"/"+fileName;
	}

	/**
	 * 判断文件是否过期
	 * 
	 * @param file
	 * @param cache
	 * @return
	 */
	public static final boolean isOverdue(Context context, File file, int cache) {
		if (System.currentTimeMillis() - file.lastModified() < cache) {
			// 没有过期
			return true;
		} else {
			// 网络状态不可用
			if (!NetUtils.isConnected(context)) {
				return true;
			}
			// 过期删除
			file.delete();
			return false;
		}
	}

	public static void deleteFile(Context context,String url,Object...obj) {
		String fileName = Math.abs(HttpUtils.getUrl(url, obj)
				.hashCode()) + ".json";
		File file = new File(FileUtils.getCachePath(context),fileName);
		// 如果文件夹存在，则删除
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 添加文件
	 * @param file
	 * @param json
	 */
	public static void NewFile(File file, String json) {
		// 如果文件夹存在，则删除
		if (file.exists()) {
			file.delete();
		}
		// 声明流对象
		FileOutputStream fos = null;
		try {
			file.createNewFile();
			// 创建流对象
			fos = new FileOutputStream(file);
			// 转换为byte数组
			byte[] b1 = json.getBytes();
			fos.write(b1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fos.close();

			} catch (Exception e) {
			}

		}
	}

	/**
	 * 读取Json 文件
	 * @param file
	 * @return
	 */
	public static String ReadJson(File file) {
		// 声明流对象
		BufferedReader burrReader = null;
		try {

			burrReader = new BufferedReader(new FileReader(file));
			String json = "";
			String temp = "";
			while ((temp = burrReader.readLine()) != null) {
				json += temp;
			}
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流，释放资源
				burrReader.close();
			} catch (Exception e) {
			}
		}
		return null;
	}
	/**
	 * 
	 * @param fileName
	 * @param toFileName
	 */
	public static void inputToOuput(String fileName,String toFileName){
		try {
			outFile(toFileName, inputFile(fileName));
		} catch (Exception e) {
		}
	}
	
	public static void loadBitmap(Context context,ImageView img,String fileName) {
//		Bitmap image = null;
//		try {
//			InputStream  fileInputStream=new FileInputStream(new File(fileName));
//			image = BitmapFactory.decodeStream(fileInputStream);
//			fileInputStream.close();
//			return image;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		ImageLoader.getInstance(context).loadImage(true, fileName, img);
//		return null;
	}
	
	
	
	/**
	 * 获取图片后缀名
	 * @param fileName
	 * @return
	 */
	public static String getSuffixName(String fileName){  
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(fileName, options);
		return options.outMimeType;
		
	}
	
	/**
	 * 写二进制到指定目录
	 * @param fileNamePath
	 * @param bytes
	 */
	public static void outFile(String fileNamePath,byte[] bytes){
		try {
			File file=new File(fileNamePath);
			//如果文件不存在
			if (!file.exists()&&bytes!=null) {
//				BufferedOutputStream bos = new BufferedOutputStream(
//						new FileOutputStream(file));
//				bos.write(bytes, 0, bytes.length);
//				bos.write(bytes);
//				bos.flush();
//				bos.close();
				
				ByteArrayInputStream inputStream=new ByteArrayInputStream(bytes);
				BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(file));
				byte[] b=new byte[1024];
				int len;
				while ((len=inputStream.read(b))!=-1) {
					bos.write(b,0,len);
//					bos.flush();
				}
				inputStream.close();
				bos.flush();
				bos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] inputFile(String fileNamePath){
		try {
			File file=new File(fileNamePath);
			//如果文件存在
			if (file.exists()) {
//				BufferedInputStream bis = new BufferedInputStream(
//						new FileInputStream(file));
//				byte[] buffer=new byte[bis.available()];
//				bis.read(buffer);
//				bis.close();
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file));
		        ByteArrayOutputStream out = new ByteArrayOutputStream(1024); 
		        byte[] temp = new byte[1024];        
		        int size = 0;        
		        while ((size = in.read(temp)) != -1) {        
		            out.write(temp, 0, size);        
		        }        
		        in.close();      
		        out.flush();
		        out.close();
		        return out.toByteArray();
//				return buffer;
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
	
	
	

}
