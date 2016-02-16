package x.java.net.socket.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * SocketServer 工作： 1）绑定端口 2）接收入站数据
 * 
 * 本类为多线程并发Socket 服务器,回显，并返回Success：[客户端内容]
 * 
 * 设计模式： 1）acceptor 建立连接 2）n handler 线程进行业务处理
 * 
 * 该设计模式缺点： 一个客户端，一个线程，大并发下资源会耗尽
 * 
 * 主要资源使用情况：
 * 	1） 64位的java 虚拟机中，一个线程默认要开1M的栈空间，并且每个线程根据需要会在堆上创建实例，大量的线程耗尽内存
 *  2） 大量的线程，cpu 调度，进行上下文切换的时间边长，影响效率
 * 
 * @author shilei
 * 
 */
public class CurrentSocketServer extends SingleThreadSocketServer {

	public CurrentSocketServer(int port) throws IOException {
		super(port);
	}

	/**
	 * 处理客户端连接及客户端消息，相当于Acceptor
	 * 
	 * @throws IOException
	 */
	@Override
	public void listen() throws IOException {
		while (true) {
			// 监听客户端连接
			Socket clientSocket = server.accept();
			new HandlerThread(clientSocket).start();
		}
	}

	/**
	 * 业务处理线程
	 * 
	 * @author shilei
	 *
	 */
	protected class HandlerThread extends Thread {
		private Socket clientSocket;

		public HandlerThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try {
				handleClientSocket(clientSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		CurrentSocketServer server = new CurrentSocketServer(9999);
		server.listen();
	}

}
