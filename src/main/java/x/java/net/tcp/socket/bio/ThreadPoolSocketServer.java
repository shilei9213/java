package x.java.net.tcp.socket.bio;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 伪异步IO模型
 * <p>
 * 主线程监听连接获取socket，线城池控制线程量
 * <p>
 * 缺点：
 * 1）处理修改成异步，单读写操作仍然是同步的，会造成阻塞，后续的操作排队，
 * 2）Acceptor 向线程池提交任务，在任务满时会发生阻塞，造成大量连接超时
 *
 * @author shilei
 */
public class ThreadPoolSocketServer extends MainThreadSocketServer {

    private ExecutorService pool;

    ThreadPoolSocketServer(int port) throws IOException {
        super(port);
        pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 10, 120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(15));
    }

    @Override
    void handle(Socket clientSocket) {
        // 受线程池大小限制，阻塞服务器
        pool.execute(() -> {
            try {
                super.handle(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolSocketServer server = new ThreadPoolSocketServer(9999);
        server.listen();
    }

}
