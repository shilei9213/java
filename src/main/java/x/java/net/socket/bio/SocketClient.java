package x.java.net.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * SocketClient 工作： 1）连接远程主机 2）发送数据 3）接受数据 4）关闭连接
 * 
 * @author shilei
 * 
 */
public class SocketClient {
	private Socket clientSocket;

	public SocketClient(String address, int port) throws UnknownHostException, IOException {
		clientSocket = new Socket(address, port);
		System.out.println("=========Client start , connect to : " + address + ":" + port);
	}

	public void request() throws IOException {
		// 服务器端响应流
		InputStream fromServer = clientSocket.getInputStream();
		// 请求服务器端数据流
		OutputStream toServer = clientSocket.getOutputStream();

		// 包装流
		PrintWriter writer = new PrintWriter(toServer);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fromServer));

		String reqMsg = "Hello Server!";
		String respMsg = "";

		writer.print(reqMsg);
		writer.flush();
		System.out.println("=========Send Server message : " + reqMsg);
		respMsg = reader.readLine();
		System.out.println("=========Receiver Server response : " + respMsg);

		writer.print("quit");
		writer.flush();
		System.out.println("=========Send quit message ! ");

		reader.close();
		writer.close();
		clientSocket.close();
		System.out.println("=========Client close ! ");
	}

	public static void main(String[] args) throws Exception {
		SocketClient client = new SocketClient("127.0.0.1", 9999);
		client.request();
	}

}
