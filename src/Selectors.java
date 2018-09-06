import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Selectors {
    public static void main(String[] args) {
        
    }
    public static void MySelector(){
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 9955));
            serverSocketChannel.configureBlocking(false);


            Selector selector = Selector.open();
            // 注册 channel，并且指定触发的事件是 Accept
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(128);
            writeBuff.put("I get it".getBytes());
            writeBuff.flip();

            while (true) {
                //阻塞至至少一个通道就绪
                selector.select();
                //生成一个SelectorKey类型的集合
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        // 创建新的连接，并且把连接注册到selector上，而且，
                        // 声明这个channel只对读操作感兴趣。
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    else if (key.isReadable()) {
                        //获取当前通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        readBuff.clear();
                        System.out.println(socketChannel.read(readBuff));
                        readBuff.flip();
                        System.out.println(new String(readBuff.array()));
                        //添加触发事件
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if (key.isWritable()) {
                        writeBuff.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.write(writeBuff);
                        //key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {

        }
    }

}
