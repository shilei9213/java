package x.java.net.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 交互协议：一行数据以 ；号结尾标志命令提交结束，发送 quit 命令标志命令终止
 *
 * @author shilei
 */
public class BIOProtocal {
    // 消息包结束，单行
    private static final String END_OF_CONTENT = ";";

    // 回话结束
    static final String QUIT_CMD = "quit";

    /**
     * 写数据
     */
    public static String read(BufferedReader reader) throws IOException {
        StringBuilder msg = new StringBuilder();
        String line = null;
        while (true) {
            line = reader.readLine();
            msg.append(line);
            if (line.endsWith(END_OF_CONTENT)) {
                msg.deleteCharAt(msg.length() - 1);
                break;
            }
        }
        return msg.toString();
    }

    /**
     * 读数据
     */
    public static void write(PrintWriter writer, String msg) {
        writer.print(msg);
        writer.println(END_OF_CONTENT);
        writer.flush();
    }

    /**
     * 关闭连接
     */
    public static void close(BufferedReader reader, PrintWriter writer) throws IOException {
        write(writer, QUIT_CMD);

        reader.close();
        writer.close();
    }
}
