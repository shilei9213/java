package x.java.net.tcp.socket.bio;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * SocketClient 工作：
 * 1）连接远程主机
 * 2）发送数据
 * 3）接受数据
 * 4）关闭连接
 * <p>
 * 协议：单行的finish作为数据发送结束
 *
 * @author shilei
 */
public class SocketClient implements Closeable {

    private Socket clientSocket;

    SocketClient(String address, int port) throws IOException {
        // 创建连接，完成三次握手
        long start = System.currentTimeMillis();

        clientSocket = new Socket(address, port);

        long end = System.currentTimeMillis();

        System.out.println("=========Client start , connect to : " + address + ":" + port + " , time cost : " + (end - start));
    }

    void request() throws IOException {
        // 服务器端响应流
        InputStream fromServer = clientSocket.getInputStream();
        // 请求服务器端数据流
        OutputStream toServer = clientSocket.getOutputStream();

        // 包装流
        PrintWriter writer = new PrintWriter(toServer);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fromServer));

        // 发送消
        String reqMsg = "Tom";
        BIOProtocal.write(writer, reqMsg);
        System.out.println("=========Send Server message : " + reqMsg);

        //接收响应
        String respMsg = BIOProtocal.read(reader);
        System.out.println("=========Receiver Server response : " + respMsg);

        BIOProtocal.close(reader, writer);

        clientSocket.close();
        System.out.println("=========Client close ! ");
    }

    public static void main(String[] args) throws Exception {
        try (SocketClient client = new SocketClient("127.0.0.1", MainThreadSocketServer.DEFAULT_PORT)) {
            client.request();
        }

    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
    }
}
