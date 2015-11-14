package x.java.concurrent.pool;

import junit.framework.TestCase;

import org.junit.Test;

import x.java.concurrent.pool.BlockingTaskQueueThreadPool;

public class BlockingTaskQueueThreadPoolTest extends TestCase {

	@Test
	public void test() {
		BlockingTaskQueueThreadPool taskQueue = new BlockingTaskQueueThreadPool(1, 5);
		for (int i = 0; i < 10; i++) {
			taskQueue.submit(new Work(i));
			System.out.println(Thread.currentThread().getName() + " add task " + i);
		}

		System.out.println(Thread.currentThread().getName() + " add finish！ ");

		taskQueue.waitTermianl();

		System.out.println("all finish！ ");
	}

	public static class Work implements Runnable {
		private int no;

		public Work(int no) {
			this.no = no;
		}

		@Override
		public void run() {

			System.out.println(Thread.currentThread().getName() + " start " + no);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " finish " + no);

		}
	}
}
