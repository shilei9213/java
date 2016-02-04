package x.java.net.socket.nio.adv;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import x.java.net.socket.nio.NIOProtocal;
import x.java.net.socket.nio.NIOProtocal.Message;

public class EchoHandler extends Handler {

	@Override
	protected void onRead(SelectionKey key) throws Exception {
		// 获得要读数据的客户端Channel
		SocketChannel clientChannel = (SocketChannel) key.channel();

		// 获取客户端
		String clientId = clientChannel.socket().getInetAddress() + ":" + clientChannel.socket().getPort();

		// 获取上次未读完的消息=======================
		Message newMessage = NIOProtocal.read(clientChannel);
		Message storeMessage = null;
		if (key.attachment() != null) {
			storeMessage = (Message) key.attachment();
			storeMessage.append(newMessage);
		} else {
			storeMessage = newMessage;
		}

		// 查看客户端是否发送完成
		if (!storeMessage.isReadFinish()) {
			key.attach(storeMessage);
			return;
		}
		// 清理之前的缓存
		key.attach(null);
		// ===========================================

		String clientMsg = storeMessage.toString();
		System.out.println("Recieve： " + clientId + " Message : " + clientMsg);
		// 写客户端
		// 检测是否关闭
		if (NIOProtocal.QUIT_CMD.equals(clientMsg)) {
			clientChannel.close();
			System.out.println("Close： " + clientId);
			return;
		}

		String respMsg = "Success : " + clientMsg;
		NIOProtocal.write(clientChannel, respMsg);
		System.out.println("Server response： " + clientId + " : " + respMsg);
	}

}
