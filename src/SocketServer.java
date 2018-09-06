import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;



public class SocketServer {
    /*
   * 在阻塞模式下一直接收socketchannel
   * */
    public static void BIO()throws IOException {

        ByteBuffer writeBuffer = ByteBuffer.allocate(8);
        ByteBuffer readBuffer = ByteBuffer.allocate(48);
        writeBuffer.put("I get it".getBytes());
        //开启并创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9955));
        while (true) {
            //accpet方法会阻塞直到获取到一个socketchannel
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("Incoming connection from" + socketChannel.socket().getRemoteSocketAddress());
            socketChannel.read(readBuffer);
            //切换读写
            readBuffer.flip();
            System.out.println(new String(readBuffer.array()));
            writeBuffer.flip();
            //向socketchannel写入数据
            socketChannel.write(writeBuffer);
        }
    }
   /*
   * 非阻塞的模式下持续接收SocketChannel
   * */
    public  void NIO()throws Exception {
        ByteBuffer writeBuffer=ByteBuffer.allocate(8);
        ByteBuffer readBuffer=ByteBuffer.allocate(48);
        writeBuffer.put("I get it".getBytes());
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9955));
        //声明为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        while (true){
            Thread.sleep(500);
            //非阻塞模式下accpet每次执行都会返回,没获取到sokcetchannel则返回null
            SocketChannel socketChannel=serverSocketChannel.accept();
            //不为空则获取了sokcetchannel
            if(socketChannel!=null){
                System.out.println("Incoming connection from"+socketChannel.socket().getRemoteSocketAddress());
                //read返回-1表示数据已经发送完毕并且对方关闭了sokcetchannel,0表示已经没有数据可以读或是readBuffer满了
                //但其实socketChannel仍然阻塞模式，并不需要对read的返回值进行处理
                while (socketChannel.read(readBuffer)!=-1){
                    readBuffer.flip();
                    System.out.println(new String(readBuffer.array()));
                    writeBuffer.flip();
                    socketChannel.write(writeBuffer);
                }
            }
        }
    }
    public  void socketChannel()throws IOException{
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9955));

        // 发送请求
        ByteBuffer buffer = ByteBuffer.wrap("1234567890".getBytes());
        socketChannel.write(buffer);

        // 读取响应
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int num;
        if ((num = socketChannel.read(readBuffer)) > 0) {
            readBuffer.flip();

            byte[] re = new byte[num];
            readBuffer.get(re);

            String result = new String(re, "UTF-8");
            System.out.println("返回值: " + result);
        }
    }

}



