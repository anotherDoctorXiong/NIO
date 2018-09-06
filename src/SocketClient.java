import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketClient {
    /*
    * 阻塞模式下的socketchannel的使用
    * */
    public void BIO()throws IOException{
        ByteBuffer writeBuffer=ByteBuffer.allocate(8);
        ByteBuffer readBuffer=ByteBuffer.allocate(48);
        writeBuffer.put("hello".getBytes());
        //开启并连接socketchannel
        SocketChannel socketChannel=SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9955));
        //切换读写模式
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        //readBudder是空的直接读
        socketChannel.read(readBuffer);
        System.out.println(socketChannel.socket().getRemoteSocketAddress()+"回复："+new String(readBuffer.array()));
    }
    /*
    * 非阻塞模式下socketchannel的使用
    * */
    public void NIO()throws IOException{
        ByteBuffer writeBuffer=ByteBuffer.allocate(8);
        ByteBuffer readBuffer=ByteBuffer.allocate(48);
        writeBuffer.put("hello".getBytes());

        SocketChannel socketChannel=SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9955));
        //声明非阻塞
        socketChannel.configureBlocking(false);
        //确保连接已经建立再执行write
        while (true){
            if(socketChannel.finishConnect()){
                writeBuffer.flip();
                socketChannel.write(writeBuffer);
                break;
            }
        }
        //处理从通道获取的数据
        while (true){
            int key=socketChannel.read(readBuffer);
            if(key!=0&&key!=-1){
                System.out.println(socketChannel.socket().getRemoteSocketAddress()+"回复："+new String(readBuffer.array()));
                break;
            }
        }
        socketChannel.close();
    }
}
