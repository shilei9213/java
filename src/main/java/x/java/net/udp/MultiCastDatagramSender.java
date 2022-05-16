package x.java.net.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * //TODO <comment>
 *
 * @author shilei05
 * @date 2022/4/19
 * @since
 */
public class MultiCastDatagramSender implements Closeable {

    MulticastSocket multicastSender;


    public MultiCastDatagramSender() throws Exception {
        this.multicastSender = new MulticastSocket();
        multicastSender.setTimeToLive(1);

        String clientId = multicastSender.getLocalAddress() + ":" + multicastSender.getLocalPort();
        System.out.println("client : " + clientId);
    }

    public static void main(String[] args) throws Exception {
        try (MultiCastDatagramSender client = new MultiCastDatagramSender()) {
            client.multicast();
        }
    }


    private void multicast() throws Exception {
        System.out.println("多播： ");

        // 使用广播地址
        InetSocketAddress address = new InetSocketAddress(MultiCastDatagramReceiver.MULTICAST_HOST,
                MultiCastDatagramReceiver.MULTICAST_PORT);
        String serverId = address.getAddress() + " : " + address.getPort();
        for (int i = 0; i < 10; i++) {
            String data = "hello" + System.currentTimeMillis();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, address);

            // 等待接收udp数据包，接收之前阻塞
            multicastSender.send(sendPacket);

            System.out.println("Send " + serverId + "Message : " + data);
        }

        byte[] dataBytes = "quit".getBytes(StandardCharsets.UTF_8);
        DatagramPacket quitPacket = new DatagramPacket(dataBytes, dataBytes.length, address);
        multicastSender.send(quitPacket);
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(multicastSender)) {
            multicastSender.close();
        }
    }
}
