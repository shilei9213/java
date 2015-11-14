package x.java.concurrent.pool.simulator;

/**
 * 可调用方法,封装实际的任务
 * 
 * @author shilei
 * 
 * @param <T>
 */
public interface Callable<T> {
	public T call();
}
