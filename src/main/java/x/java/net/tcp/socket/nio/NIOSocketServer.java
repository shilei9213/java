package x.java.net.tcp.socket.nio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import x.java.net.tcp.socket.nio.NIOProtocal.Message;

/**
 * 非阻塞IO模型
 * <p>
 * nio socket ，
 * <p>
 * 采用 Reactor 模式:
 * 异步事件监听，当在Selector中注册对Channel感兴趣的事件后，当一批事件发生时，会调用响应的方法处理
 * <p>
 * 注：该模式处理方法需要自定义，客户端需要操作读写过程。
 * <p>
 * 注2：单线程处理所有事件，此处没有引入多线程来提高网络性能，设计模式直接影响网络延迟
 * <p>
 * 事件：
 * <p>
 * （1）SelectionKey.OP_CONNECT —— 连接就绪事件，表示客户与服务器的连接已经建立成功
 * </p>
 * <p>
 * （2）SelectionKey.OP_ACCEPT —— 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
 * </p>
 * <p>
 * （3）SelectionKey.OP_READ —— 读就绪事件，"内核态"socket的读缓冲区已经有数据，可以通过read操作复制到"用户态"
 * 缓冲区读写，读写完成返回0 ， 连接关闭返回-1
 * <p>
 * （4）SelectionKey.OP_WRITE
 * ——写就绪事件，"内核态"socket的写缓冲区已经有空闲，可以通过write操作写缓冲区，如果socket写缓冲区没有空闲的话，同床会阻塞，</>
 * <p>
 * <p>
 * ====================================================
 * SelectionKey对象是用来跟踪注册事件的句柄
 * 。一个key和一个channel与selector是一一对应的，必须在一个channel上注册某个selector的key
 * 在SelectionKey对象的有效期间，Selector会一直监控与SelectionKey对象相关的事件
 * ，如果事件发生，就会把SelectionKey对象加入到selected-keys集合中。
 * <p>
 * 在以下情况下，SelectionKey对象会失效，这意味着Selector再也不会监控与它相关的事件：
 * 程序调用SelectionKey的cancel()方法 关闭与SelectionKey关联的Channel
 * 与SelectionKey关联的Selector被关闭
 *
 * @author shilei
 */
public class NIOSocketServer implements Closeable {

    private boolean runnable = true;

    // 类似于serversocket的职能
    private ServerSocketChannel server;

    // 单线程的事件选择器
    private Selector selector;

    NIOSocketServer(int port) throws IOException {
        // 创建监听Channel
        initServer(port);

        // 创建Selector
        selector = Selector.open();

        // 先ServerSocketChannel 中注册感兴趣的事件
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("=========Server start , listen: " + port);
    }

    // 创建监听 channel
    private void initServer(int port) throws IOException {
        // 创建可选择通道指定了TCP通讯协议
        server = ServerSocketChannel.open();

        // 设置非阻塞模式
        server.configureBlocking(false);

        // 从通道中获取ServerSocket，绑定端口
        server.socket().bind(new InetSocketAddress(port));
    }

    /**
     * 循环监听事件
     */
    void listen() {
        while (runnable) {
            try {
                // 监听Channel上的一批阻塞事件,本方法会阻塞，指导有一批事件到来
                // 此处存在NIO select 空轮询现象
                int keyCount = selector.select(1000);

                if (keyCount == 0) {
                    continue;
                }

                // 事件到达
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 将已选择键从键值中删除
                    iterator.remove();
                    // 处理该key
                    processKey(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processKey(SelectionKey key) throws Exception {
        try {
            if (key.isAcceptable()) {
                System.out.println("=== isAcceptable() ： " + key);
                // 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
                onAccept(key);
            } else if (key.isConnectable()) {
                // 连接就绪事件，表示客户与服务器的连接已经建立成功
                System.out.println("=== isConnectable() ： " + key);
            } else if (key.isReadable()) {
                System.out.println("=== isReadable() :  " + key);
                // 读就绪事件，"内核态"socket的读缓冲区已经有数据，可以通过read操作复制到"用户态" 缓冲区读写，读写完成返回0，
                // 连接关闭返回-1
                onRead(key);
            } else if (key.isWritable()) {
                // SelectionKey.OP_WRITE
                // ——写就绪事件，"内核态"socket的写缓冲区已经有空闲，可以通过write操作写缓冲区，如果socket写缓冲区没有空闲的话，同床会阻塞
                System.out.println("=== isWritable() ： " + key);

            }
        } catch (Throwable e) {
            onException(key, e);
        }
    }

    /**
     * 产生异常时触发
     */
    private void onException(SelectionKey key, Throwable cause) throws IOException {
        if (cause instanceof IOException) {
            IOException e = (IOException) cause;
            String msg = e.getMessage();
            if (msg != null && msg.contains("Connection reset by peer")) {
                System.out.println("onException: Connection reset by peer");
                key.cancel();
                key.channel().close();
            }
        }
    }

    /**
     * 有连接时触发
     */
    private void onAccept(SelectionKey key) throws IOException {
        // 获取时间对应的Channel,因为只有ServerSocketChannel 注册了 accept时间，所以可以强制转换；
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

        // 有客户端连接，创建针对该客户机的Channel
        SocketChannel clientChannel = serverChannel.accept();
        String clientId = clientChannel.socket().getInetAddress() + ":" + clientChannel.socket().getPort();

        // 客户机的Channel设置为异步模式
        clientChannel.configureBlocking(false);

        // 客户机的Channel 在selector中注册客户端事件，channel是全双工
        System.out.println("=========Accept new client connect : " + clientId);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }



    // 处理数据读取
    private void onRead(SelectionKey key) throws IOException {
        // 获得要读数据的客户端Channel
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // 获取客户端
        String clientId = clientChannel.socket().getInetAddress() + ":" + clientChannel.socket().getPort();

        // 获取上次未读完的消息=======================
        Message newMessage = NIOProtocal.read(clientChannel);
        Message storeMessage = null;
        // key.attachment 可以缓存上次未处理完的读取
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

        // 返回客户端
        // 这里由于没有设计中间缓存写服务队列，所以，这里直接写出，当写缓冲区没有空间时，会阻塞
        String respMsg = "Success : " + clientMsg;
        NIOProtocal.write(clientChannel, respMsg);
        System.out.println("Server response： " + clientId + " : " + respMsg);
    }


    public static void main(String[] args) throws Exception {
        try(NIOSocketServer server = new NIOSocketServer(9999)){
            server.listen();
        }
    }

    @Override
    public void close() throws IOException {
        runnable = false;
        if (selector != null) {
            selector.close();
        }
        if (server != null) {
            server.close();
        }
    }
}
