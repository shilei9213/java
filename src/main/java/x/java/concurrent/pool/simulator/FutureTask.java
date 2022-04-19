package x.java.concurrent.pool.simulator;

/**
 *
 * 本质：堆上线程间共享结果封装 ****
 *
 * Future在Java5就引入了。
 *
 * 优点：一定程度上让一个线程池内的任务异步执行了
 * 缺点：传统回调最大的问题就是不能将控制流分离到不同的事件处理器中。例如主线程等待各个异步执行的线程返回的结果来做下一步操作，则必须阻塞在future.get()的地方等待结果返回。这时候又变成同步了。
 *
 * CompletableFuture在Java8引入。
 *
 * 实现了Future和CompletionStage接口，保留了Future的优点，并且弥补了其不足。即异步的任务完成后，需要用其结果继续操作时，无需等待。可以直接通过thenAccept、thenApply、thenCompose
 * 等方式将前面异步处理的结果交给另外一个异步事件处理线程来处理。
 * 可见，这种方式才是我们需要的异步处理。一个控制流的多个异步事件处理能无缝的连接在一起。
 *
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
