package x.java.net.socket.protocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 交互协议
 * 
 * @author shilei
 *
 */
public class Protocal {
	// 消息包结束，单行
	public static String END_FLAG = "finish";

	// 回话结束
	public static String CLOSE = "quit";

	/**
	 * 写数据
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String read(BufferedReader reader) throws IOException {
		StringBuilder msg = new StringBuilder();
		String line = null;
		while (true) {
			line = reader.readLine();
			if (END_FLAG.equals(line)) {
				break;
			}
			msg.append(line);
		}
		return msg.toString();
	}

	/**
	 * 读数据
	 * 
	 * @param writer
	 * @param msg
	 */
	public static void write(PrintWriter writer, String msg) {
		writer.println(msg);
		writer.println(END_FLAG);
		writer.flush();
	}

	/**
	 * 关闭连接
	 * 
	 * @param reader
	 * @param writer
	 * @throws IOException
	 */
	public static void close(BufferedReader reader, PrintWriter writer) throws IOException {
		writer.println(CLOSE);
		writer.println(END_FLAG);
		writer.flush();

		reader.close();
		writer.close();
	}
}
