package zxq.org.superxutil.net;

/**
 * 网络链接状态
 * @author 朱侠强
 */
public enum HttpErrorEunm {
	/**
	 * 网络连接不可用
	 */
	NetworkNotAvailable(ErrorResult.NETWORK_NOT_AVAILABLE),
	/**
	 * 加载超时
	 */
	NetworkError(ErrorResult.NETWORK_NOT_ERROR),
	/**
	 * 数据解析异常
	 */
	DataFormat(ErrorResult.DEFAULT),
	/**
	 * 未知异常
	 */
	Unknown(ErrorResult.DEFAULT),
	/**
	 * 数据为空
	 */
	DataNull(ErrorResult.DATA_NULL),
	Loading(""),
	Complete("");
	
	private String errorResult;
	private HttpErrorEunm(String errorResult){
		this.errorResult=errorResult;
	}
	public String getErrorResult() {
		return errorResult;
	}
	public void setErrorResult(String errorResult) {
		this.errorResult = errorResult;
	}



	public static final class ErrorResult{
		public static final String NETWORK_NOT_AVAILABLE="网络连接不可用!";
		public static final String NETWORK_NOT_ERROR="服务器连接失败!";
		public static final String DATA_NULL="暂无数据";
		public static final String DEFAULT="发生错误,请稍后重试!";
	}
	

}
