package x.java.net.socket.nio.adv;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class Reactor {
	// 多路复用器
	private Selector selector;

	// 业务处理器
	private Handler handler;

	public Reactor(Handler handler) {
		if (handler == null) {
			new IllegalArgumentException("Handler can not be null . ");
		}

		this.handler = handler;

		// 出事后多路复用器
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Selector getSelector() {
		return selector;
	}

	/**
	 * 启动
	 * 
	 * @throws IOException
	 */
	public void run() {
		while (true) {
			try {
				// 监听Channel上的一批阻塞事件,本方法会阻塞，指导有一批事件到来
				// 此处存在NIO select 空轮询现象
				int keyCount = selector.select();

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
			}
		}
	}

	private void processKey(SelectionKey key) throws Exception {
		try {
			if (key.isAcceptable()) {
				System.out.println("=== isAcceptable() ： " + key);
				// 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
				handler.onAccept(key);
			} else if (key.isConnectable()) {
				// 连接就绪事件，表示客户与服务器的连接已经建立成功
				System.out.println("=== isConnectable() ： " + key);
				handler.onConnect(key);
			} else if (key.isReadable()) {
				System.out.println("=== isReadable() :  " + key);
				// 读就绪事件，"内核态"socket的读缓冲区已经有数据，可以通过read操作复制到"用户态" 缓冲区读写，读写完成返回0，
				// 连接关闭返回-1
				handler.onRead(key);
			} else if (key.isWritable()) {
				// SelectionKey.OP_WRITE
				// ——写就绪事件，"内核态"socket的写缓冲区已经有空闲，可以通过write操作写缓冲区，如果socket写缓冲区没有空闲的话，同床会阻塞
				System.out.println("=== isWritable() ： " + key);
				handler.onWrite(key);
			}
		} catch (Throwable e) {
			handler.onException(key, e);
		}
	}
}
