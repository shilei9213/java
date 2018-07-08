package x.java.concurrent.pool.demo;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueueDemo：DelayQueue是一个无界阻塞队列，只有在延迟期满时才能从中提取元素。该队列的头部是延迟期满后保存时间最长的Delayed 元素
 * https://www.cnblogs.com/jobs/archive/2007/04/27/730255.html
 *
 * a) 关闭空闲连接。服务器中，有很多客户端的连接，空闲一段时间之后需要关闭之。
 * b) 缓存。缓存中的对象，超过了空闲时间，需要从缓存中移出。
 * c) 任务超时处理。在网络协议滑动窗口请求应答式交互时，处理超时未响应的请求。
 *
 * @author shilei0907
 * @version 1.0, 2018/7/8
 */
public class DelayQueueDemo {


    public static void main(String[] args) {
        DelayQueue<DelayTask> delayQueue = new DelayQueue<>();

        delayQueue.add(new DelayTask("1", 10L, TimeUnit.SECONDS));
        delayQueue.add(new DelayTask("2", 15L, TimeUnit.SECONDS));
        delayQueue.add(new DelayTask("3", 20L, TimeUnit.SECONDS));

        while (!delayQueue.isEmpty()) {
            try {
                DelayTask task = delayQueue.take();
                System.out.println(task);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class DelayTask implements Delayed {

        private String name;
        private long executeTime;

        public DelayTask(String name, long delay, TimeUnit timeUnit) {
            this.name = name;
            this.executeTime = System.currentTimeMillis() + timeUnit.toMillis(delay);
        }


        //返回剩余时间；用来判断最前面订单出队的时间
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        // 比较优先级：该方法是用来比较队列里面的订单的顺序
        @Override
        public int compareTo(Delayed o) {
            if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
                return 1;
            } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
                return -1;
            }
            return 0;
        }

        @Override
        public String toString() {
            return "DelayTask{" +
                    "name='" + name + '\'' +
                    ", executeTime=" + executeTime +
                    '}';
        }
    }

}
