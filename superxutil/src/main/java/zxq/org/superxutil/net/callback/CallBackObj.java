package zxq.org.superxutil.net.callback;

/**
 * 解析对象
 * @param <T>
 */
public interface CallBackObj<T> extends CallBack {
    void onRequestCompleteObj(T obj);
}
