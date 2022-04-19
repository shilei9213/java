package x.java.net.tcp.socket.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * 同步阻塞IO模型
 * <p>
 * 主线程监听连接获取socket，处理工作线程执行
 *
 * <p>
 * 该设计模式缺点： 一个客户端，一个线程，大并发下资源会耗尽
 * <p>
 * 主要资源使用情况：
 * 1） 64位的java 虚拟机中，一个线程默认要开1M的栈空间，并且每个线程根据需要会在堆上创建实例，大量的线程耗尽内存
 * 2） 大量的线程，cpu 调度，进行上下文切换的时间边长，影响效率
 *
 * @author shilei
 */
public class SingleThreadSocketServer extends MainThreadSocketServer {

    SingleThreadSocketServer(int port) throws IOException {
        super(port);
    }

    /**
     * 处理包在线程中执行
     */
    @Override
    void handle(Socket clientSocket) {
        new Thread(() -> {
            try {
                super.handle(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }


    public static void main(String[] args) throws Exception {
        SingleThreadSocketServer server = new SingleThreadSocketServer(9999);
        server.listen();
    }

}
