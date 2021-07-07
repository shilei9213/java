package x.java.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;

/**
 * file：queue.meta 元数据，格式 head:int,tail:int
 * file: queue.data 保存队列内容 length:int , data:byte[]
 */
public class FileQueue {
    private static final Charset charset = Charset.forName("UTF-8");

    private static final String ROOT_PATH = "./";
    private static final String META_FILE = "queue.meta";
    private static final String DATA_FILE = "queue.data";

    private MappedByteBuffer dataBuffer = null;
    private MappedByteBuffer metaBuffer = null;

    private static final int BUFFER_SIZE = 10 * 1024;

    private int head;
    private int tail;

    public FileQueue() {

        try (RandomAccessFile metaFile = new RandomAccessFile(ROOT_PATH + File.separator + META_FILE, "rw");
                RandomAccessFile dataFile = new RandomAccessFile(ROOT_PATH + File.separator + DATA_FILE, "rw");) {
            metaBuffer = metaFile.getChannel().map(MapMode.READ_WRITE, 0, 8);

            head = metaBuffer.getInt();
            tail = metaBuffer.getInt();

            System.out.println("meta : " + head + "," + tail);
            // 从队列头部开始map
            dataBuffer = dataFile.getChannel().map(MapMode.READ_WRITE, head, BUFFER_SIZE);

            // buffer 归位
            tail -= head;
            head = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean add(byte[] message) {
        int length = message.length;
        int dataSize = length + 4;
        if (!hasSpace(dataSize)) {
            return false;
        }

        // 写数据
        dataBuffer.position(tail);
        dataBuffer.putInt(length);
        dataBuffer.put(message);

        // meta更新
        tail += dataSize;

        // 写buffer 持久化 tail
        metaBuffer.putInt(4, tail);

        return true;
    }

    public byte[] get() {
        if (isEmpty()) {
            return null;
        }

        // 读取数据
        dataBuffer.position(head);
        int length = dataBuffer.getInt();
        byte[] content = new byte[length];
        dataBuffer.get(content);

        // 更新头
        head += length + 4;
        metaBuffer.putInt(0, head);

        return content;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public boolean hasSpace(int size) {
        return tail + size < BUFFER_SIZE;
    }

    public static void main(String[] args) {
        FileQueue fileQueue = new FileQueue();

        for (int i = 0; i < 1000; i++) {
            String message = "message" + i;
            boolean result = fileQueue.add(message.getBytes(charset));
            System.out.println("send : " + message + " ," + result);
        }

        byte[] data = null;
        while ((data = fileQueue.get()) != null) {
            String message = new String(data, charset);
            System.out.println("receive : " + message);
        }

        System.out.println("finish ! ");
    }

}