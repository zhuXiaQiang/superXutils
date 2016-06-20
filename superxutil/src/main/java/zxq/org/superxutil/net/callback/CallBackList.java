package zxq.org.superxutil.net.callback;

import java.util.List;

/**
 * 解析List数据
 */
public interface CallBackList<T> extends CallBack{
    void onRequestCompleteList(List<T> list);
}
