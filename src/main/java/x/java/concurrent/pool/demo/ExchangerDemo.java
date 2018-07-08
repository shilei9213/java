package x.java.concurrent.pool.demo;

import java.util.concurrent.Exchanger;

/**
 * Exchanger: Exchanger（交换者）是一个用于线程间协作的工具类。Exchanger用于进行线程间的数据交换。它提供一个同步点，在这个同步点两个线程可以交换彼此的数据。这两个线程通过exchange方法交换数据，
 * 如果第一个线程先执行exchange方法，它会一直等待第二个线程也执行exchange，当两个线程都到达同步点时，这两个线程就可以交换数据，将本线程生产出来的数据传递给对方。
 *
 * @author shilei0907
 * @version 1.0, 2018/7/8
 */
public class ExchangerDemo {
    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();

        new Task("1", exchanger).start();
        new Task("2", exchanger).start();
        new Task("3", exchanger).start();
        new Task("4", exchanger).start();
    }

    static class Task extends Thread {

        private String name;
        private Exchanger<String> exchanger;


        public Task(String name, Exchanger<String> exchanger) {
            this.name = name;
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            try {
                System.out.println(name + " : start");
                String result = exchanger.exchange(name);
                System.out.println(name + " : " + result);
                System.out.println(name + " : finish ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
