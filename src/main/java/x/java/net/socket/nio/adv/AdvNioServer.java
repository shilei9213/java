package x.java.net.socket.nio.adv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * 启动 ， 监听端口，绑定接受事件
 * 
 * @author shilei
 *
 */
public class AdvNioServer implements Runnable {
	private int port;

	private Acceptor acceptor;
	private Dispatcher dispatcher;

	public AdvNioServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		// 获得服务器通道
		try {
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(new InetSocketAddress(this.port), 10);

			acceptor = new Acceptor(server);
			dispatcher = new Dispatcher(1);
			// 接入业务处理
			dispatcher.setHandler(new EchoHandler());
			// 启动各个组件
			startComponents();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startComponents() {
		// 连接器
		acceptor.run();
		System.out.println("Acceptor 启动完成");

		// 调度器
		dispatcher.run();
		System.out.println("Dispatcher 启动完成");
	}

	public static void main(String[] args) {
		AdvNioServer server = new AdvNioServer(9999);
		server.run();
	}
}
