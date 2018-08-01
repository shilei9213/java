package x.java.concurrent.pool.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Future Demo
 *
 * @author shilei0907
 * @version 1.0, 2018/8/1
 */
public class FutureDemo {
    public static void main(String[] args) throws Exception {
        new FutureDemo().run();
    }

    private void run() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " : start task");

                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(Thread.currentThread().getName() + " ：task finish !");
            }
        };

        Future<?> result = executor.submit(task);

        TimeUnit.SECONDS.sleep(1);

        // 取消任务
        System.out.println(Thread.currentThread().getName() + " start cancel!  ");
        result.cancel(true);
        System.out.println(Thread.currentThread().getName() + " all Finish! ");

        executor.shutdownNow();
    }
}
