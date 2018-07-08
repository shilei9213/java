package x.java.concurrent.pool.demo;

import java.util.concurrent.Semaphore;

/**
 * Semaphore：Semaphore（信号量）是用来控制同时访问特定资源的线程数量，它通过协调各个线程，以保证合理的使用公共资源。
 * <p>
 * 控制并发的两种思路：
 * 1）任务进队列，起固定并发线程数处理，线程数不会增加
 * 2）线程不限，使用信号量控制可执行的线程数
 *
 * @author shilei0907
 * @version 1.0, 2018/7/8
 */
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(2);

        for (int i = 0; i < 10; i++) {
            new Task(String.valueOf(i), semaphore).start();
        }
    }

    static class Task extends Thread {
        private String name;
        private Semaphore semaphore;

        public Task(String name, Semaphore semaphore) {
            this.name = name;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {

            try {
                semaphore.acquire();
                System.out.println(name + " : do job1");
                System.out.println(name + " : do job2");
                System.out.println(name + " : do job3");
                System.out.println(name + " : do job4");
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
