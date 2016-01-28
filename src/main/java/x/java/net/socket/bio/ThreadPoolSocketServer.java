package x.java.net.socket.bio;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SocketServer 工作： 1）绑定端口 2）接收入站数据
 * 
 * 本类为多线程并发Socket 服务器,回显，并返回Success：[客户端内容]
 * 
 * 设计模式： 1）acceptor 建立连接 2）n handler 线程进行业务处理 使用线程池避免线程耗尽的情况
 * 
 *缺点：	
 *	1）处理修改成异步，单读写操作仍然是同步的，会造成阻塞，后续的操作排队，
 * 	2）Acceptor 向线程池提交任务，在任务满时会发生阻塞，造成大量连接超时
 * 
 * 
 * @author shilei
 * 
 */
public class ThreadPoolSocketServer extends CurrentSocketServer {
	private final ThreadPool pool;

	public ThreadPoolSocketServer(int port) throws IOException {
		super(port);
		pool = new ThreadPool(10, 15);
	}

	@Override
	public void listen() throws IOException {
		while (true) {
			// 监听客户端连接
			Socket clientSocket = server.accept();
			pool.execute(new HandlerThread(clientSocket));
		}
	}

	private class ThreadPool {
		private ExecutorService executor;

		public ThreadPool(int maxPoolSize, int maxTaskSize) {
			// 线程池
			executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(maxTaskSize));
		}

		public void execute(Runnable r) {
			executor.execute(r);
		}
	}
	
	public static void main(String[] args) throws Exception{
		ThreadPoolSocketServer server = new ThreadPoolSocketServer(9999);
		server.listen();
	}

}
