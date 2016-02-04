package x.java.net.socket.nio.adv;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 接入
 * 
 * @author shilei
 *
 */
public class Acceptor {
	// 反应器
	private Reactor acceptReactor;

	// 服务器通道
	private ServerSocketChannel serverChannel;

	// 调度器
	private Dispatcher dispatcher;

	public Acceptor(ServerSocketChannel serverChannel) throws ClosedChannelException {
		this.acceptReactor = new Reactor(new AcceptHandler());
		this.serverChannel = serverChannel;
	}

	public void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					serverChannel.register(acceptReactor.getSelector(), SelectionKey.OP_ACCEPT);
					acceptReactor.run();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}, "BossThread").start();
	}

	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * 连接处理器
	 * 
	 * @author shilei
	 *
	 */
	private class AcceptHandler extends Handler {

		@Override
		protected void onException(SelectionKey key, Throwable e) {
			try {
				e.printStackTrace();
				key.channel().close();
				key.cancel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		protected void onAccept(SelectionKey key) throws IOException {
			// 获取时间对应的Channel,因为只有ServerSocketChannel 注册了 accept时间，所以可以强制转换；
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

			// 有客户端连接，创建针对该客户机的Channel
			SocketChannel clientChannel = serverChannel.accept();
			String clientId = clientChannel.socket().getInetAddress() + ":" + clientChannel.socket().getPort();

			// 客户机的Channel设置为异步模式
			clientChannel.configureBlocking(false);
			// 客户机的Channel 在selector中注册客户端事件
			System.out.println("=========Accept new client connect : " + clientId);

			dispatcher.submint(clientChannel, key);
		}

	}
}
