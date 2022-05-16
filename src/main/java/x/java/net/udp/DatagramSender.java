package x.java.net.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * //TODO <comment>
 *
 * @author shilei05
 * @date 2022/4/19
 * @since
 */
public class DatagramSender implements Closeable {

    DatagramSocket sender;


    public DatagramSender() throws Exception {
        this.sender = new DatagramSocket();

        String clientId = sender.getLocalAddress() + ":" + sender.getLocalPort();
        System.out.println("client : " + clientId);
    }

    public static void main(String[] args) throws Exception {
        try (DatagramSender client = new DatagramSender()) {
            client.multicast();
        }
    }

    private void unicast() throws Exception {
        System.out.println("单播： ");
        InetSocketAddress address = new InetSocketAddress("localhost", DatagramReceiver.DEFAULT_PORT);
        String serverId = address.getAddress() + " : " + address.getPort();
        for (int i = 0; i < 10; i++) {
            String data = "hello" + System.currentTimeMillis();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, address);

            // 等待接收udp数据包，接收之前阻塞
            sender.send(sendPacket);

            System.out.println("Send " + serverId + "Message : " + data);
        }

        byte[] dataBytes = "quit".getBytes(StandardCharsets.UTF_8);
        DatagramPacket quitPacket = new DatagramPacket(dataBytes, dataBytes.length, address);
        sender.send(quitPacket);
    }

    private void broadcast() throws Exception {
        System.out.println("广播： ");
        //广播地址 255.255.255.255。另外ipv6不支持广播，必须找到默认端口，才能使用
        String broadcastHost = "255.255.255.255";
        InetSocketAddress address = new InetSocketAddress(broadcastHost, DatagramReceiver.DEFAULT_PORT);
        String serverId = address.getAddress() + " : " + address.getPort();
        for (int i = 0; i < 10; i++) {
            String data = "hello" + System.currentTimeMillis();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, address);

            // 等待接收udp数据包，接收之前阻塞
            sender.send(sendPacket);

            System.out.println("Send " + serverId + "Message : " + data);
        }

        byte[] dataBytes = "quit".getBytes(StandardCharsets.UTF_8);
        DatagramPacket quitPacket = new DatagramPacket(dataBytes, dataBytes.length, address);
        sender.send(quitPacket);
    }

    private void multicast() throws Exception {
        System.out.println("组播： ");

        /*
         * DatagramSocket 实例可用于发送或接收多播数据报包。发送多播数据报不必加入多播组。然而，在发送多播数据报包之前，应该首先使用 setOption 和 StandardSocketOptions.IP_MULTICAST_IF
         * 配置发送多播数据报的默认传出接口
         */
        NetworkInterface outgoingIf = NetworkInterface.getByName("lo0");
        sender.setOption(StandardSocketOptions.IP_MULTICAST_IF, outgoingIf);
        sender.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1);

        /*
         * 设置多播组
         */
        InetAddress mcastaddr = InetAddress.getByName(MultiCastDatagramReceiver.MULTICAST_HOST);
        InetSocketAddress dest = new InetSocketAddress(mcastaddr, 9999);
        String serverId = dest.getAddress() + " : " + dest.getPort();

        for (int i = 0; i < 10; i++) {
            String data = "hello" + System.currentTimeMillis();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, dest);

            // 等待接收udp数据包，接收之前阻塞
            sender.send(sendPacket);

            System.out.println("Send " + serverId + "Message : " + data);
        }

        byte[] dataBytes = "quit".getBytes(StandardCharsets.UTF_8);
        DatagramPacket quitPacket = new DatagramPacket(dataBytes, dataBytes.length, dest);
        sender.send(quitPacket);
    }


    @Override
    public void close() throws IOException {
        if (Objects.nonNull(sender)) {
            sender.close();
        }
    }
}
