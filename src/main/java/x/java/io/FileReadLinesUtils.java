package x.java.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Java 逐行读取
 * 
 * @author shilei
 *
 */
public class FileReadLinesUtils {

	/**
	 * IO逐行读取
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static int readLines(File file) throws Exception {
		int lines = 0;

		FileReader fr = new FileReader(file);
		BufferedReader bufferedreader = new BufferedReader(fr);

		while (bufferedreader.readLine() != null) {
			lines++;
		}
		fr.close();

		return lines;
	}

	/**
	 * nio 逐行读取测试
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static int readLinesNio(File file) throws Exception {
		int lines = 0;

		return lines;
	}

	/**
	 * 文件映射读取小于2G的文件
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static int readLinesByMapedFromSmallFile(File file) throws Exception {
		int lines = 0;

		FileInputStream fis = new FileInputStream(file);
		FileChannel fileChannel = fis.getChannel();

		StringBuffer line = new StringBuffer();
		MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
		while (buffer.hasRemaining()) {
			char c = (char) buffer.get();
			if (c == '\n') {
				lines++;
				System.out.println(line);
				line = new StringBuffer();
				continue;
			}

			line.append(c);
		}

		fis.close();
		return lines;

	}

	/**
	 * 内存映射读取测试,文件超过2G
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static int readLinesByMaped(File file) throws Exception {
		int lines = 0;

		FileInputStream fis = new FileInputStream(file);
		FileChannel fileChannel = fis.getChannel();

		// 文件大小
		long fileLen = fileChannel.size();
		// 默认映射大小
		long maxMapSize = 1024 * 1024 * 1024;

		StringBuffer line = new StringBuffer();

		long position = 0;
		long size = maxMapSize;

		// 由于内存映射要消耗2倍的内存空间，且分配大小不能超快2G，所以，多次映射超过2G的大文件要多次映射才会完成
		// pos 映射部分的开始位置，size 映射大小
		while (position < fileLen) {
			// 1 判读本次映射是否超过文件大小
			if (position + maxMapSize > fileLen) {
				size = fileLen - position;
			}

			// 2 文件映射
			MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY, position, size);

			// 3 文件处理
			while (buffer.hasRemaining()) {
				char c = (char) buffer.get();
				if (c == '\n') {
					lines++;
					System.out.println(line);
					line = new StringBuffer();
					continue;
				}
				line.append(c);
			}

			// 调整偏移
			position += maxMapSize;
		}
		if (line.length() > 0) {
			lines++;
			System.out.println(line);
		}

		fis.close();
		return lines;
	}

	/**
	 * 直接内存读取
	 * 
	 * @param file
	 * @return
	 */
	public static int readLinesMem(File file) throws Exception {
		int lines = 0;

		FileInputStream fis = new FileInputStream(file);
		FileChannel fileChannel = fis.getChannel();

		// 文件大小
		StringBuffer line = new StringBuffer();

		// 开发一个同文件大小一样的内存映射区域
		ByteBuffer buffer = ByteBuffer.allocateDirect((int) fileChannel.size());

		while (fileChannel.read(buffer) != -1) {
			// 转成读模式
			buffer.flip();

			// 获取数据
			while (buffer.hasRemaining()) {
				char c = (char) buffer.get();
				if (c == '\n') {
					lines++;
					System.out.println(line);
					line = new StringBuffer();
					continue;
				}
				line.append(c);
			}
			// 清空
			buffer.clear();
		}

		fis.close();
		return lines;
	}

	// -------------------------------------------------------------------
	public static void testWriteThread() throws Exception {
		String filename = "/Users/shilei/Root/Develop/DevelopSpace/Test/thread.txt";
		FileOutputStream fos = new FileOutputStream(filename);
		FileChannel fchannel = fos.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1014 * 1024);

		for (int i = 0; i < buffer.capacity() / 2; i++) {
			buffer.putChar('一');
		}

		buffer.flip();
		fchannel.write(buffer);

		fos.close();

	}

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		FileReadLinesUtils.testWriteThread();
		long end = System.currentTimeMillis();

		System.out.println("时间：" + (end - start));
	}
}
