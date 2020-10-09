package x.java.concurrent.pool.demo;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * ForkJoinQueueDemo：任务分解 fork() 多线程执行， 最后合并结果，类似于 mr
 * <p>
 * 任务类型：ForkJoinTask 以下是他的子类
 * RecursiveAction：用于没有返回结果的任务
 * RecursiveTask ：用于有返回结果的任务
 */
public class ForkJoinQueueDemo {
    public static void main(String[] args) {
        String artical = "a b c d e f \na b c d e f";

        ForkJoinPool pool = new ForkJoinPool();

        WordCountTask task = new WordCountTask(artical);

        int result = pool.invoke(task);

        System.out.println("result : " + result);
    }

    static class WordCountTask extends RecursiveTask<Integer> {
        private String message;

        public WordCountTask(String message) {
            this.message = message;
        }

        @Override
        protected Integer compute() {
            String[] lines = message.split("\n");
            if (lines.length == 1) {
                return lines[0].split(" ").length;
            }

            WordCountTask[] wordCountTasks = new WordCountTask[lines.length];
            for (int i = 0; i < lines.length; i++) {
                wordCountTasks[i] = new WordCountTask(lines[i]);
                wordCountTasks[i].fork();
            }

            int result = 0;
            for (WordCountTask wordCountTask : wordCountTasks) {
                result += wordCountTask.join();
            }
            return result;
        }
    }
}
