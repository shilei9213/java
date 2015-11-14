package x.java.net.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOSocketServer {
	final private Charset charset = Charset.forName("UTF-8");// 创建UTF-8字符集

	private ServerSocketChannel server;
	private Selector selector;

	public NIOSocketServer(int port) throws IOException {
		// 创建可选择通道
		server = ServerSocketChannel.open();
		// 设置非阻塞模式
		server.configureBlocking(false);

		// 从通道中获取ServerSocket，绑定端口
		ServerSocket serverSocker = server.socket();
		serverSocker.bind(new InetSocketAddress(port));

		// 创建Selector
		selector = Selector.open();
		// 先channel 中注册感兴趣的事件
		server.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("=========Server start , listen: " + port);
	}

	/**
	 * 循环监听事件
	 */
	public void listen() {
		while (true) {
			try {
				// 监听Channel上的一批阻塞事件,本方法会阻塞，指导有一批事件到来
				int keyCount = selector.select();
				if (keyCount == 0) {
					continue;
				}
				// 事件到达
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					// 将已选择键从键值中删除
					iterator.remove();
					// 处理该key
					processKey(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processKey(SelectionKey key) throws IOException {
		if (!key.isValid()) {
			return;
		}
		// 接受请求事件
		if (key.isAcceptable()) {
			handleAccept(key);
		} else if (key.isReadable()) {
			handleMsg(key);
		}
	}

	private void handleAccept(SelectionKey key) throws IOException {
		System.out.println("=========Accept client connect : ");
		// 获取时间对应的Channel
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		// 有客户端连接，创建针对该客户机的Channel
		SocketChannel clientChannel = serverChannel.accept();
		// 客户机的Channel设置为异步模式
		clientChannel.configureBlocking(false);
		// 客户机的Channel 在selector中注册客户端事件
		clientChannel.register(selector, SelectionKey.OP_READ);
	}

	// 客户端有读事件
	private void handleMsg(SelectionKey key) throws IOException {
		// 获得要读数据的客户端Channel
		SocketChannel clientChannel = (SocketChannel) key.channel();

		// 读取数据到buffer
		ByteBuffer buffer = ByteBuffer.allocate(200);
		try {
			long bufferSize = clientChannel.read(buffer);

			// 数据读取完成，退出
			if (bufferSize == -1) {
				closeKey(key);
				return;
			}
		} catch (Exception e) {
			//客户端强制关闭连接是，清理key，退出处理
			e.printStackTrace();
//			closeKey(key);
			return;
		}

		// 反转，从头处理Buffer
		buffer.flip();
		String clientMsg = charset.decode(buffer).toString();

		if (clientMsg != null) {
			if (clientMsg.toLowerCase().contains("quit")) {
				closeKey(key);
				return;
			}

			System.out.println("=========Recieve Client Message : " + clientMsg);
			String respMsg = "Success : " + clientMsg;
			ByteBuffer respBuffer = charset.encode(CharBuffer.wrap(respMsg));
			clientChannel.write(respBuffer);
			System.out.println("=========Server response : " + respMsg);
		}
		// 将key对应的Channel设置成准备下一次读取
		key.interestOps(SelectionKey.OP_READ);
	}

	private void closeKey(SelectionKey key) throws IOException {
		System.out.println("=========Server close ! ");
		key.cancel();
		key.channel().close();
	}

	public static void main(String[] args) throws Exception {
		NIOSocketServer server = new NIOSocketServer(9999);
		server.listen();
	}

}
