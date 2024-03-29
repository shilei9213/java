package x.java.net.tcp.socket.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 交互协议：一行数据以 ；号结尾标志命令提交结束，发送 quit 命令标志命令终止
 * 
 * @author shilei
 *
 */
public class AIOProtocal {
	private static final Charset charset = Charset.forName("UTF-8");// 创建UTF-8字符集

	// 消息包结束，单行
	public static String END_OF_CONTENT = ";\r\n";

	// 回话结束
	public static String QUIT_CMD = "quit";

	/**
	 * 写数据
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static Message read(ByteBuffer buffer) {
		Message clientMsg = new Message();

		buffer.flip();
		// 解密消息
		String msg = charset.decode(buffer).toString();

		// 组装消息
		clientMsg.append(msg);

		buffer.clear();

		return clientMsg;
	}

	/**
	 * 读数据
	 * 
	 * @param writer
	 * @param msg
	 * @throws IOException
	 */
	public static ByteBuffer write(String msg)  {
		return charset.encode(msg + END_OF_CONTENT);
	}

	/**
	 * 关闭连接
	 * 
	 * @param reader
	 * @param writer
	 * @throws IOException
	 */
	public static ByteBuffer close()  {
		return write(QUIT_CMD);
	}

	// 消息对象
	public static class Message {
		private StringBuilder content = new StringBuilder();
		private boolean isReadFinish;

		public Message() {

		}

		public Message(String content, boolean isReadFinish) {
			this.content.append(content);
			this.isReadFinish = isReadFinish;
		}

		public boolean isReadFinish() {
			return isReadFinish;
		}

		public StringBuilder getContent() {
			return content;
		}

		public void setContent(StringBuilder content) {
			this.content = content;
		}

		public void setReadFinish(boolean isReadFinish) {
			this.isReadFinish = isReadFinish;
		}

		public void append(Message msg) {
			if (msg != null) {
				this.content.append(msg.getContent());
				this.isReadFinish = msg.isReadFinish();
			}
		}

		public void append(String msg) {
			this.content.append(msg);
			if (content.toString().endsWith(END_OF_CONTENT)) {
				content.delete(content.length() - 3, content.length());
				isReadFinish = true;
			}
		}

		public String toString() {
			return content.toString();
		}
	}
}
