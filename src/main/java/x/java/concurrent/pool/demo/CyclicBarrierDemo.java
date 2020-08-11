package x.java.concurrent.pool.demo;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier: 可循环使用（Cyclic）的屏障（Barrier), 让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活。
 * <p>
 * CountDownLatch的计数器只能使用一次。
 * 而CyclicBarrier的计数器可以使用reset() 方法重置。
 * 所以CyclicBarrier能处理更为复杂的业务场景，比如如果计算发生错误，可以重置计数器，并让线程们重新执行一次
 *
 * @author shilei0907
 * @version 1.0, 2018/7/8
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) throws Exception {
        int taskCount = 4;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(taskCount);

        for (int i = 1; i < taskCount; i++) {
            new Task(String.valueOf(i), cyclicBarrier).start();
        }

        cyclicBarrier.await();
        System.out.println("all finish! ");
    }

    static class Task extends Thread {

        private String name;
        private CyclicBarrier cyclicBarrier;

        public Task(String name, CyclicBarrier cyclicBarrier) {
            this.name = name;
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            try {
                //阻塞
                System.out.println(name + " do finish wait orther! ");

                cyclicBarrier.await();

                System.out.println(name + " clean  ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

}
