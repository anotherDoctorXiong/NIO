
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;


public class IO {
    /*
    *通过Buffer读取文件
    * */
    public static void ChannelToBuffer() throws IOException{
        RandomAccessFile file=new RandomAccessFile("E:/java/hello.txt","rw");

        //创建filechannel
        FileChannel channel=file.getChannel();

        //创建缓冲区
        ByteBuffer buf=ByteBuffer.allocate(48);
        //ByteBuffer buf2=ByteBuffer.wrap(array[]);等效于上面创建缓冲区

        //将channel的数据读入buffer,返回的是进入buffer的数据长度,-1就是channel数据发送完毕
        int key=channel.read(buf);
        while (key!=-1){

            //flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值
            buf.flip();

            //只要limit大于position
            while (buf.hasRemaining())
                //获取position位置上的内容
                System.out.println(buf.get());

            //令position=0
            buf.clear();

            //将channel未读取的数据读入buf,position=limit时key=0,所以一定要先调用clear()
            key=channel.read(buf);
        }
        file.close();
    }
    /*
    * 通过Buffer写入文件
    * */
    public static void BufferToChannel() throws IOException{
        RandomAccessFile file=new RandomAccessFile("E:/java/hello.txt","rw");
        FileChannel channel=file.getChannel();
        byte[] b={61,74,65};
        ByteBuffer buf=ByteBuffer.wrap(b);
        channel.write(buf);
        file.close();
    }
    /*
    * channel间的数据传导
    * */
    public static void ChannelTransfrom() {
        try {
            RandomAccessFile fromFile = new RandomAccessFile("E:/java/hello1.txt", "rw");
            FileChannel      fromChannel = fromFile.getChannel();

            RandomAccessFile toFile = new RandomAccessFile("E:/java/hello2.txt", "rw");
            FileChannel      toChannel = toFile.getChannel();

            long position = 0;
            long count = fromChannel.size();
            toChannel.transferFrom(fromChannel,0,count);
            //fromChannel.transferTo(0,toChannel.size(),toChannel);和上面那条语句等效，都是把一个channel加入到另一个channel中

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

