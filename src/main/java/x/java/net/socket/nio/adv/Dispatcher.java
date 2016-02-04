package x.java.net.socket.nio.adv;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Dispatcher {
	// 反应堆
	private Reactor[] reactors;
	private int reactorPointer;
	// 业务
	private Handler handler;
	// 线程池
	private Thread[] threads;

	public Dispatcher(int threadCount) {
		reactors = new Reactor[threadCount];
		threads = new Thread[threadCount];

		for (int i = 0; i < threadCount; i++) {
			reactors[i] = new Reactor(handler);
			threads[i] = new Thread(new ThreadWork(reactors[i]), "work_thread_" + i);
		}
	}

	public void run() {
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
	}

	private Reactor dispatch() {
		reactorPointer++;
		if (reactorPointer == reactors.length) {
			reactorPointer = 0;
		}

		return reactors[reactorPointer];
	}

	/**
	 * 设置Handler
	 * 
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public synchronized void submint(SocketChannel clientChannel, SelectionKey key) throws ClosedChannelException {
		Reactor reactor = dispatch();
		clientChannel.register(reactor.getSelector(), SelectionKey.OP_READ);
	}

	private class ThreadWork implements Runnable {
		private Reactor reactor;

		public ThreadWork(Reactor reactor) {
			this.reactor = reactor;
		}

		public void run() {
			reactor.run();
		}

	}

}
