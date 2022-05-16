package x.java.net.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author shilei05
 * @date 2022/4/19
 */
public class MultiCastDatagramReceiver implements Closeable {

    //其中永久的组播地址：224.0.0.0-224.0.0.2。而剩下的就是临时组了：224.0.1.0～224.0.1.255是公用组播地址
    static final String MULTICAST_HOST = "239.0.1.255";
    static final int MULTICAST_PORT = 9999;

    MulticastSocket multicastReceiver;


    public MultiCastDatagramReceiver() throws Exception {
        this.multicastReceiver = new MulticastSocket(9999);

        String receiverId = multicastReceiver.getLocalSocketAddress() + ":" + multicastReceiver.getLocalPort();
        System.out.println("Server : " + receiverId);

        // 也就是只接受239.0.1.255这个地址的人发来的消息
        InetAddress mcastaddr = InetAddress.getByName(MULTICAST_HOST);
        InetSocketAddress group = new InetSocketAddress(mcastaddr, 8888);
        NetworkInterface netIf =  NetworkInterface.getByName("en0");
        // 加入多播组
        multicastReceiver.joinGroup(group, netIf);
    }

    public static void main(String[] args) throws Exception {
        try (MultiCastDatagramReceiver server = new MultiCastDatagramReceiver()) {
            server.start();
        }
    }

    private void start() throws Exception {
        while (true) {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // 等待接收udp数据包，接收之前阻塞
            multicastReceiver.receive(receivePacket);
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
        if (Objects.nonNull(multicastReceiver)) {
            multicastReceiver.close();
        }
    }
}
