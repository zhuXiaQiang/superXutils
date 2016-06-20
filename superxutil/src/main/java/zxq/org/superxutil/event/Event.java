package zxq.org.superxutil.event;


import zxq.org.superxutil.net.HttpErrorEunm;

/**
 * 事件处理总类
 * @author zxq
 * 
 */
public class Event {
	/**
	 * 网络加载状态通知
	 * @author 朱侠强
	 */
	public static class EventLoadEunm {
		private HttpErrorEunm httpErrorEunm;
		private String url;
//		private String clazzName;
		private Object[] parames;
		
		public Object[] getParames() {
			return parames;
		}

		public void setParames(Object[] parames) {
			this.parames = parames;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public HttpErrorEunm getHttpErrorEunm() {
			return httpErrorEunm;
		}

		public void setHttpErrorEunm(HttpErrorEunm httpErrorEunm) {
			this.httpErrorEunm = httpErrorEunm;
		}

//		public String getClazzName() {
//			return clazzName;
//		}
//
//		public void setClazzName(String clazzName) {
//			this.clazzName = clazzName;
//		}

		public EventLoadEunm(HttpErrorEunm httpErrorEunm, String url) {
			super();
			this.httpErrorEunm = httpErrorEunm;
			this.url = url;
//			this.clazzName = clazzName;
		}
	}



}
