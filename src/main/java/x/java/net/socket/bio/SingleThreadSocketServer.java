package x.java.net.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SocketServer 工作： 1）绑定端口 2）接收入站数据
 * 
 * 本类为单线程Socket 服务器,回显，并返回Success：[客户端内容]
 * 
 * @author shilei
 * 
 */
public class SingleThreadSocketServer {
	private ServerSocket server;

	public SingleThreadSocketServer(int port) throws IOException {
		server = new ServerSocket();
		server.bind(new InetSocketAddress(port));
		System.out.println("=========Server start , listen: " + port);
	}

	/**
	 * 处理客户端连接及客户端消息
	 * 
	 * @throws IOException
	 */
	public void listen() throws IOException {
		Socket clientSocket = server.accept();
		System.out.println("=========Accept client connect : ");
		// 读客户端发送数据流
		InputStream fromClient = clientSocket.getInputStream();
		// 回写客户端数据
		OutputStream toClient = clientSocket.getOutputStream();

		// 包装流
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				fromClient));
		PrintWriter writer = new PrintWriter(toClient);

		// 读取客户端数据并回写客户端
		while (true) {
			String msg = reader.readLine();
			// 如果msg 为quit 则退出，关闭流程
			if (msg != null && msg.equalsIgnoreCase("quit")) {
				break;
			}
			System.out.println("=========Recieve Client Message : " + msg);
			String respMsg = "Success : " + msg;
			writer.println(respMsg);
			writer.flush();
			System.out.println("=========Server response : " + respMsg);
		}

		fromClient.close();
		toClient.close();
		clientSocket.close();
		server.close();

		System.out.println("=========Server close ! ");
	}

	public static void main(String[] args) throws Exception {
		SingleThreadSocketServer server = new SingleThreadSocketServer(9999);
		server.listen();
	}

}
