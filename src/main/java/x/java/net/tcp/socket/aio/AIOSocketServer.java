package x.java.net.tcp.socket.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

import x.java.net.tcp.socket.aio.AIOProtocal.Message;
import x.java.net.tcp.socket.nio.NIOProtocal;

/**
 * aio socket server
 * 
 * 
 * @author shilei
 *
 */
public class AIOSocketServer implements Closeable {

	private CountDownLatch runnable= new CountDownLatch(1);

	private final AsynchronousServerSocketChannel serverChannel;


	public static void main(String[] args) throws Exception {
		try(AIOSocketServer server = new AIOSocketServer(9999)){
			server.run();
		}
	}

	public AIOSocketServer(int port) throws IOException {
		// 启动服务器，绑定协议
		serverChannel = AsynchronousServerSocketChannel.open();
		// 绑定端口,后面的参数是backlog
		serverChannel.bind(new InetSocketAddress(port), 100);

		System.out.println("=========Server start , listen: " + port);
	}

	private void run() throws InterruptedException {
		// 等待连接
		doAccept();

		// 主线程阻塞
		runnable.await();
	}

	private void doAccept() {
		// 等待连接,如果有客户端连接成功，系统自动回调定义的CompletionHandler，
		// 其中 accept接收两个参数，第一个是attachment 回调传递给回调方法，第二个是handler，handler 泛型接收 channel 和 attachment
		serverChannel.accept(serverChannel, new AcceptCompletionHandler());
	}


	/**
	 * 连接完成事件
	 * 
	 * 连接完成处理器 CompletionHandler<V,A> ：
	 *
	 * V 必须是AsynchronousSocketChannel 是已建立连接完成的channel， 为了完成绑定后续建立连接的功能，
	 * A 必须能获取到AsynchronousServerSocketChannel
	 * 
	 * @author shilei
	 *
	 */
	private class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

		/**
		 * 连接完成事件
		 */
		@Override
		public void completed(AsynchronousSocketChannel clientChannel, AsynchronousServerSocketChannel serverChannel) {
			try {
				// 继续注册监听其他连接
				serverChannel.accept(serverChannel, this);

				// 处理当前链接
				String clientId = clientChannel.getRemoteAddress().toString();
				System.out.println("=========Accept new client connect : " + clientId);

				// 读客户端数据
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				// 注册读监听事件,并将核心的几个数据 buffer ，clientChannel 和 一些自定义参数附件，通过构造函数或其他的方式传递到handler中
				clientChannel.read(buffer, clientChannel, new ReadCompletionHandler(buffer, null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 建立连接时如果发现错误，则抛出异常
		 */
		@Override
		public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
			exc.printStackTrace();
		}
	}

	/**
	 * 读完成Handler ， 在缓冲区有数据的时候回调
	 * 
	 * @author shilei
	 *
	 */
	private class ReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
		private ByteBuffer buffer;
		private Message message;

		public ReadCompletionHandler(ByteBuffer buffer, Message message) {
			this.buffer = buffer;
			this.message = message;
		}

		@Override
		public void completed(Integer result, AsynchronousSocketChannel clientChannel) {
			try {
				// 读取信息片段
				Message msgSegment = AIOProtocal.read(buffer);
				// 组包
				if (this.message == null) {
					this.message = msgSegment;
				} else {
					this.message.append(msgSegment);
				}

				// 是否分包未结束，继续等待客户端数据
				if (!msgSegment.isReadFinish()) {
					buffer.clear();
					clientChannel.read(buffer, clientChannel, this);
					// 退出
					return;
				}

				// 整个消息处理结束
				String clientId = clientChannel.getRemoteAddress().toString();
				String clientMsg = message.toString();
				System.out.println("Recieve： " + clientId + " Message : " + clientMsg);

				// 写客户端
				// 检测是否关闭
				if (NIOProtocal.QUIT_CMD.equals(clientMsg)) {
					clientChannel.close();
					System.out.println("Close： " + clientId);
					return;
				}

				// 回写
				String returnMsg = "Success : " + message.toString();
				ByteBuffer writeBuffer = AIOProtocal.write(returnMsg);

				clientChannel.write(writeBuffer, clientChannel, new WriteCompletionHandler(writeBuffer));
				System.out.println("Server response ： " + returnMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel clientChannel) {
			exc.printStackTrace();
		}

	}

	/**
	 * 写完成handler
	 * 
	 * @author shilei
	 *
	 */
	private class WriteCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
		private ByteBuffer buffer;

		public WriteCompletionHandler(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void completed(Integer result, AsynchronousSocketChannel clientChannel) {
			if (buffer.hasRemaining()) {
				clientChannel.write(buffer, clientChannel, this);
			} else {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				clientChannel.read(readBuffer, clientChannel, new ReadCompletionHandler(readBuffer, null));
			}
		}

		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel clientChannel) {
			exc.printStackTrace();
		}

	}

	@Override
	public void close() throws IOException {
		runnable.countDown();
		if(serverChannel!=null){
			serverChannel.close();
		}
	}



}
