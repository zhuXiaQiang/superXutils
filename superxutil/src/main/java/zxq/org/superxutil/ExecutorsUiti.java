package zxq.org.superxutil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 * @author 朱侠强
 */
public class ExecutorsUiti {
	static final int nThreadsImg = 2;
	static final int nThreads = 3;
	private static ExecutorService executorService;
	private static ExecutorService executorServiceImg;

	public static ExecutorService getExecutorService() {
		
		if (executorService == null) {
			synchronized (ExecutorsUiti.class) {
				if (executorService==null) {
					executorService = Executors.newFixedThreadPool(nThreads);
				}
			}
		}
		return executorService;
	}

	public static ExecutorService getExecutorServiceImg() {
		if (executorServiceImg == null) {
			synchronized (ExecutorsUiti.class) {
				if (executorServiceImg == null) {
					executorServiceImg = Executors.newFixedThreadPool(nThreadsImg);
				}
			}
			
		}
		return executorServiceImg;
	}

	public static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
