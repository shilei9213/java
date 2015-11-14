package x.java.net.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SocketServer 工作： 1）绑定端口 2）接收入站数据
 * 
 * 本类为多线程并发Socket 服务器,回显，并返回Success：[客户端内容]
 * 
 * @author shilei
 * 
 */
public class CurrentSocketServer {

	private ServerSocket server;

	public CurrentSocketServer(int port) throws IOException {
		server = new ServerSocket(port);
		System.out.println("=========Server start , listen: " + port);
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
				System.out.println("=========Accept client connect : ");
				// 读客户端发送数据流
				InputStream fromClient = clientSocket.getInputStream();
				// 回写客户端数据
				OutputStream toClient = clientSocket.getOutputStream();

				// 包装流
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fromClient));
				PrintWriter writer = new PrintWriter(toClient);

				// 读取客户端数据并回写客户端
				while (true) {
					String msg = reader.readLine();
					// 如果msg 为quit 则退出，关闭流程
					if (msg != null && msg.equalsIgnoreCase("quit")) {
						break;
					}
					System.out.println("=========Recieve Client Message : "
							+ msg);
					String respMsg = "Success : " + msg;
					writer.println(respMsg);
					writer.flush();
					System.out.println("=========Server response : " + respMsg);
				}

				fromClient.close();
				toClient.close();
				clientSocket.close();

				System.out.println("=========Server Socket close ! ");
				System.out.println("===============================");
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
