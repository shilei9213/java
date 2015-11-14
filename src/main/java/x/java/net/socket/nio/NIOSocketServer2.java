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

public class NIOSocketServer2 {
	final private Charset charset = Charset.forName("UTF-8");// 创建UTF-8字符集

	private ServerSocketChannel server;
	private Selector selector;

	public NIOSocketServer2(int port) throws IOException {
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
		StringBuilder msgBuilder = new StringBuilder();
		ByteBuffer buffer = ByteBuffer.allocate(200);
		// 读取本次所有数据，clientChannel.read(buffer) > 0 表示读取到本次结束
		while (clientChannel.read(buffer) > 0) {
			// 反转，从头处理Buffer
			buffer.flip();
			msgBuilder.append(charset.decode(buffer).toString());
		}

		// 处理消息
		String msg = msgBuilder.toString();
		if (!msg.equals("")) {
			if (msg.toLowerCase().contains("quit")) {
				closeKey(key);
				return;
			}

			System.out.println("=========Recieve Client Message : " + msg);
			String respMsg = "Success : " + msg;
			ByteBuffer respBuffer = charset.encode(CharBuffer.wrap(msg));
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
		NIOSocketServer2 server = new NIOSocketServer2(9999);
		server.listen();
	}

}
