package x.java.net.tcp.socket.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO client
 * 
 * @author shilei
 *
 */
public class AIOSocketClient implements Closeable {
	private CountDownLatch runnable= new CountDownLatch(1);

	private String serverIp;
	private int port;


	public static void main(String[] args) throws Exception {
		try(AIOSocketClient client = new AIOSocketClient("127.0.0.1", 9999)){
			client.run();
		}
	}

	public AIOSocketClient(String serverIp, int port) {
		this.serverIp = serverIp;
		this.port = port;
	}

	public void run() throws IOException, InterruptedException {
		// 创建通道
		AsynchronousSocketChannel serverChannel = AsynchronousSocketChannel.open();
		// 建立连接
		serverChannel.connect(new InetSocketAddress(serverIp, port), serverChannel, new ConnectCompletionHandler());

		runnable.await();
	}

	@Override
	public void close() throws IOException {
		runnable.countDown();
	}

	/**
	 * 连接完成处理
	 * 
	 * @author shilei
	 *
	 */
	private class ConnectCompletionHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {

		@Override
		public void completed(Void result, AsynchronousSocketChannel serverChannel) {
			try {
				String server = serverChannel.getRemoteAddress().toString();
				System.out.println("=========Client start , connect to : " + server);

				String reqMsg = "Tom";
				ByteBuffer writeBuffer = AIOProtocal.write(reqMsg);
				serverChannel.write(writeBuffer, serverChannel, new WriteCompletionHandler(writeBuffer, false));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
			exc.printStackTrace();
		}

	}

	/**
	 * 写完成事件处理
	 * 
	 * @author shilei
	 *
	 */
	private class WriteCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

		private ByteBuffer buffer;
		private boolean isClose;

		public WriteCompletionHandler(ByteBuffer buffer, boolean isClose) {
			this.buffer = buffer;
			this.isClose = isClose;
		}

		@Override
		public void completed(Integer result, AsynchronousSocketChannel serverChannel) {
			try {
				if (buffer.hasRemaining()) {
					serverChannel.write(buffer, serverChannel, this);
				} else {
					if (isClose) {
						serverChannel.close();
						System.out.println("=========Client close ! ");
						close();
						return;
					}
					// 继续读取
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					serverChannel.read(readBuffer, serverChannel, new ReadCompletionHandler(readBuffer));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
			exc.printStackTrace();
		}
	}

	/**
	 * 读完成事件
	 * 
	 * @author shilei
	 *
	 */
	private class ReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
		private ByteBuffer buffer;

		public ReadCompletionHandler(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void completed(Integer result, AsynchronousSocketChannel serverChannel) {
			String respMsg = AIOProtocal.read(buffer).toString();
			System.out.println("=========Receiver Server response : " + respMsg);

			// 关闭
			ByteBuffer closeBuffer = AIOProtocal.close();
			serverChannel.write(closeBuffer, serverChannel, new WriteCompletionHandler(closeBuffer, true));
		}

		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel serverChannel) {
			exc.printStackTrace();
		}

	}


}
