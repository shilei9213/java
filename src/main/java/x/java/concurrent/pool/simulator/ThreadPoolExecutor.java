package x.java.concurrent.pool.simulator;

import java.util.LinkedList;
import java.util.List;

/**
 * 线程池，增加数据使用
 * 
 * @author shilei
 * 
 */
public class ThreadPoolExecutor {
	// 任务队列
	private List<FutureTask<?>> taskList = new LinkedList<FutureTask<?>>();

	// 任务启动标志
	private boolean isRunnable = true;

	// 工作线程
	private Thread thread = null;

	public ThreadPoolExecutor() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isRunnable) {
					try {
						while (taskList.isEmpty()) {
							synchronized (this) {
								wait();
							}
						}
						FutureTask<?> task = taskList.remove(0);
						task.run();
					} catch (InterruptedException e) {
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});
		thread.start();

	}

	public void submit(FutureTask<?> task) {
		synchronized (this) {
			taskList.add(task);
			notifyAll();
		}
	}

	/**
	 * 停止所有的工作线程
	 */
	public void shutdown() {
		isRunnable = false;
		thread.interrupt();
	}
}
