package x.java.net.tcp.socket.nio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import x.java.net.tcp.socket.nio.NIOProtocal.Message;

/**
 * 客户端建立连接，写入数据
 */
public class NIOSocketClient implements Closeable {

	private boolean runnable = true;

	// 客户端通道
	private SocketChannel clientChannel = null;

	// 单线程的事件选择器
	private Selector selector;

	public static void main(String[] args) throws Exception {
		try(NIOSocketClient client = new NIOSocketClient("127.0.0.1", 9999)){
			client.run();
		}
	}


	NIOSocketClient(String serverIp, int port) throws IOException {
		InetSocketAddress address = new InetSocketAddress(serverIp, port);
		connect(address);
	}

	private void connect(InetSocketAddress address) throws IOException{
		// 获得一个客户端通道
		clientChannel = SocketChannel.open();
		// 设置通道为异步
		clientChannel.configureBlocking(false);

		// 创建单线程选择器
		selector = Selector.open();

		// 连接远程服务器
		boolean connect = clientChannel.connect(address);
		if(connect){
			// 注册读取事件
			clientChannel.register(selector, SelectionKey.OP_READ);
			// 向server 发送数据
			doWrite();
		}else{
			// 注册连接事件
			clientChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}


	private void run() {
		while (runnable) {
			try {
				// 监听Channel上的一批阻塞事件,本方法会阻塞，指导有一批事件到来
				int keyCount = selector.select(1000);

				if (keyCount == 0) {
					continue;
				}

				System.out.println("=========Select keys : " + keyCount);
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
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private void processKey(SelectionKey key) throws Exception {
		try {
			if (key.isConnectable()) {
				// 连接就绪事件，表示客户与服务器的连接已经建立成功
				System.out.println("=== isConnectable() ： " + key);
				onConnect(key);
			} else if (key.isReadable()) {
				System.out.println("=== isReadable() :  " + key);
				// 读就绪事件，"内核态"socket的读缓冲区已经有数据，可以通过read操作复制到"用户态" 缓冲区读写，读写完成返回0，
				// 连接关闭返回-1
				onRead(key);
			}
		} catch (Throwable e) {
			onException(key, e);
		}
	}

	/**
	 * 产生异常时触发

	 */
	private void onException(SelectionKey key, Throwable cause) throws IOException {
		if (cause instanceof IOException) {
			IOException e = (IOException) cause;
			String msg = e.getMessage();
			if (msg != null && msg.indexOf("Connection reset by peer") != -1) {
				System.out.println("onException: Connection reset by peer");
				key.channel().close();
				return;
			}
		}
	}

	/**
	 * 通常用于客户端使用，连接成功时触发
	 */
	private void onConnect(SelectionKey key) throws IOException {

		// 如果正在连接，则完成连接
		if (clientChannel.isConnectionPending()) {
			clientChannel.finishConnect();
		}

		clientChannel.configureBlocking(false);


		// 注册读事件
		clientChannel.register(selector, SelectionKey.OP_READ);

		// 向服务器发送数据
		doWrite();
	}

	private void doWrite() throws IOException{
		// 发送消
		String reqMsg = "Tom";
		NIOProtocal.write(clientChannel, reqMsg);
		System.out.println("=========Send Server message : " + reqMsg);
	}

	// 处理数据读取
	private void onRead(SelectionKey key) throws IOException {
		// 获得要读数据的客户端Channel
		SocketChannel clientChannel = (SocketChannel) key.channel();

		// 接收响应
		Message respMsg = NIOProtocal.read(clientChannel);
		System.out.println("=========Receiver Server response : " + respMsg);

		NIOProtocal.close(clientChannel);
		System.out.println("=========Client close ! ");
	}


	@Override
	public void close() throws IOException {
		runnable = false;
		if(clientChannel!=null){
			clientChannel.close();
		}
	}
}
