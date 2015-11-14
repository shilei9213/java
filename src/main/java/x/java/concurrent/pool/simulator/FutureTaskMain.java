package x.java.concurrent.pool.simulator;

public class FutureTaskMain {
	public static void main(String[] args) {
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {

			@Override
			public String call() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "task finish!";
			}

		});

		ThreadPoolExecutor pool = new ThreadPoolExecutor();
		pool.submit(task);

		System.out.println("Main Thread do other work!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(task.get());

		pool.shutdown();
	}
}
