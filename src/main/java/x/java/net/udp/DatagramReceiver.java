package x.java.net.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author shilei05
 * @date 2022/4/19
 */
public class DatagramReceiver implements Closeable {
    static final int DEFAULT_PORT = 9999;

    DatagramSocket receiver;

    public DatagramReceiver() throws Exception {
        // 初始化 socket 绑定 端口，完成后可以在 9999端口上 接收广播
        this.receiver = new DatagramSocket(DEFAULT_PORT);
        String serverId = receiver.getLocalSocketAddress() + ":" + receiver.getLocalPort();
        System.out.println("Server : " + serverId);
    }

    public static void main(String[] args) throws Exception {
        try (DatagramReceiver server = new DatagramReceiver()) {
            server.start();
        }
    }

    private void start() throws Exception {
        while (true) {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // 等待接收udp数据包，接收之前阻塞
            receiver.receive(receivePacket);
            String clientId = receivePacket.getSocketAddress() + ":" + receivePacket.getPort();
            String data = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), StandardCharsets.UTF_8);
            System.out.println("Recieve： " + clientId + " Message : " + data);

            if ("quit".equals(data)) {
                break;
            }
        }
    }


    @Override
    public void close() throws IOException {
        if (Objects.nonNull(receiver)) {
            receiver.close();
        }
    }
}
