package x.java.concurrent.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞任务队列线程池
 * 
 * @author shilei
 *
 */
public class BlockingTaskQueueThreadPool {
	private BlockingQueue<Runnable> taskQueue;
	private ThreadPoolExecutor threadPool;

	public BlockingTaskQueueThreadPool(int threadPoolSize, int taskQueueSize) {
		taskQueue = new LinkedBlockingQueue<Runnable>(taskQueueSize);
		threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue, new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if (!executor.isShutdown()) {
					try {
						executor.getQueue().put(r);
					} catch (InterruptedException e) {
					}
				}
			}

		});
	}

	/**
	 * 提交任务，会阻塞
	 * 
	 * @param task
	 */
	public void submit(Runnable task) {
		threadPool.execute(task);
	}

	/**
	 * 等待结束
	 */
	public void waitTermianl() {
		threadPool.shutdown();

		try {
			while (!threadPool.isTerminated()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
	}
}
