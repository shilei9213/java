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
 * 该模式主要缺点：所有客户端同步，一次只能处理一个连接
 * 
 * @see BIOProtocal
 * 
 * @author shilei
 * 
 */
public class SingleThreadSocketServer {
	protected ServerSocket server;

	public SingleThreadSocketServer(int port) throws IOException {
		// 初始化 server：指定了TCP通讯协议
		server = new ServerSocket();
		// 绑定端口 启动
		server.bind(new InetSocketAddress(port));
		System.out.println("=========Server start , listen: " + port);
	}

	/**
	 * 处理客户端连接及客户端消息
	 * 
	 * @throws IOException
	 */
	protected void listen() throws IOException {
		while (true) {
			// 获得连接socket,此时完成三次握手
			Socket clientSocket = server.accept();
			// 处理连接消息
			handleClientSocket(clientSocket);
		}
	}

	protected void handleClientSocket(Socket clientSocket) throws IOException {
		String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
		System.out.println("=========Accept client connect : " + clientId);

		// 读客户端发送数据流
		InputStream fromClient = clientSocket.getInputStream();
		// 回写客户端数据
		OutputStream toClient = clientSocket.getOutputStream();

		// 包装流
		BufferedReader reader = new BufferedReader(new InputStreamReader(fromClient));
		PrintWriter writer = new PrintWriter(toClient);

		// 读取客户端数据并回写客户端
		/**
		 * 特别注意，本
		 */
		while (true) {
			// 获取client 信息
			String clientMsg = BIOProtocal.read(reader);
			System.out.println("Recieve： " + clientId + " Message : " + clientMsg);

			// 检测是否关闭
			if (BIOProtocal.QUIT_CMD.equals(clientMsg)) {
				break;
			}

			String respMsg = "Success : " + clientMsg;
			BIOProtocal.write(writer, respMsg);
			System.out.println("Server response： " + clientId + " : " + respMsg);
		}
		clientSocket.close();
		reader.close();
		writer.close();

		System.out.println(clientId + " finish ! --------------------------------------------------");
	}

	public static void main(String[] args) throws Exception {
		SingleThreadSocketServer server = new SingleThreadSocketServer(9999);
		server.listen();
	}

}
