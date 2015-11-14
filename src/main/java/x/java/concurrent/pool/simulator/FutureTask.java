package x.java.concurrent.pool.simulator;

/**
 * 封装任务，增加获得处理结果的方法
 * 
 * @author shilei
 * 
 * @param <T>
 */
public class FutureTask<T> {
	private Callable<T> callable;
	private T result;
	private boolean isFinish;

	public FutureTask(Callable<T> callable) {
		this.callable = callable;
	}

	/**
	 * 获得响应结果，如果响应失败则阻塞
	 * 
	 * @return
	 */
	public T get() {
		while (!isFinish) {
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 执行任务
	 */
	public void run() {
		result = callable.call();
		isFinish = true;
		synchronized (this) {
			notifyAll();
		}
	}
}
