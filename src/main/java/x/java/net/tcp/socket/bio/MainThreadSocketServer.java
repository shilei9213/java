package x.java.net.tcp.socket.bio;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * SocketServer 工作： acceptor模式
 * 1）绑定端口
 * 2）接收入站数据
 * <p>
 * 本类为单线程Socket 服务器,回显，并返回Success：[客户端内容]
 * <p>
 * 该模式主要缺点：所有客户端同步阻塞执行，一次只能处理一个连接
 *
 * @author shilei
 * @see BIOProtocal
 */
public class MainThreadSocketServer implements Closeable {

    static final int DEFAULT_PORT = 9999;

    // server实例
    protected ServerSocket server;

    MainThreadSocketServer() throws IOException {
        this(DEFAULT_PORT);
    }

    MainThreadSocketServer(int port) throws IOException {
        server = new ServerSocket();

        // 绑定端口 启动
        server.bind(new InetSocketAddress(port));

        System.out.println("=========Server start , listen: " + port);
    }

    /**
     * 处理客户端连接及客户端消息
     */
    void listen() throws IOException {
        while (true) {
            // 建立连接，完成三次握手，返回socket
            Socket clientSocket = server.accept();
            // 处理连接消息
            handle(clientSocket);
        }
    }

    void handle(Socket clientSocket) throws IOException {

        String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        System.out.println("=========Accept client connect : " + clientId);

        // 读客户端发送数据流
        InputStream fromClient = clientSocket.getInputStream();
        // 回写客户端数据
        OutputStream toClient = clientSocket.getOutputStream();

        // 包装流
        BufferedReader reader = new BufferedReader(new InputStreamReader(fromClient));
        PrintWriter writer = new PrintWriter(toClient);

        // 读取客户端数据并回写客户端

        while (true) {
            // 获取client 信息
            // **************************************************
            // 特别注意：这个方法会阻塞，直到有数据，限制整个服务器的线程数量和并发量
            // **************************************************
            String clientMsg = BIOProtocal.read(reader);
            System.out.println("Recieve： " + clientId + " Message : " + clientMsg);

            // 检测是否关闭
            if (BIOProtocal.QUIT_CMD.equals(clientMsg)) {
                break;
            }

            String respMsg = "Success : " + clientMsg;

            BIOProtocal.write(writer, respMsg);
            System.out.println("Server response： " + clientId + " : " + respMsg);
        }
        clientSocket.close();
        reader.close();
        writer.close();

        System.out.println(clientId + " finish ! --------------------------------------------------");
    }


    public static void main(String[] args) throws Exception {
        // 启动
        try (MainThreadSocketServer server = new MainThreadSocketServer()) {
            server.listen();
        }
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(server)) {
            server.close();
        }
    }
}
