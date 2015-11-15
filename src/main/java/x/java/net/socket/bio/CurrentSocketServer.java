package x.java.net.socket.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * SocketServer 工作： 1）绑定端口 2）接收入站数据
 * 
 * 本类为多线程并发Socket 服务器,回显，并返回Success：[客户端内容]
 * 
 * @author shilei
 * 
 */
public class CurrentSocketServer extends SingleThreadSocketServer{

	public CurrentSocketServer(int port) throws IOException {
		super(port);
	}

	/**
	 * 处理客户端连接及客户端消息
	 * 
	 * @throws IOException
	 */
	public void listen() throws IOException {
		while (true) {
			// 监听客户端连接
			Socket clientSocket = server.accept();
			new ClientSocketThread(clientSocket).start();
		}
	}

	private class ClientSocketThread extends Thread {
		private Socket clientSocket;

		public ClientSocketThread(Socket clientSocket) {
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
