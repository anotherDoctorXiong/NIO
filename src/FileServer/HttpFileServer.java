package FileServer;

import Netty.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.Date;

public class HttpFileServer {
    private static final String DEFAULT_URL = "/";
    public void run(int port,String url){
        EventLoopGroup group=new NioEventLoopGroup();
        ServerBootstrap bootstrap=new ServerBootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new FilesServerHandler(url));
                        }
                    });
            ChannelFuture future=bootstrap.bind("127.0.0.1",port).sync();
            System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://127.0.0.1:" + port + url);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args)throws Exception {
        System.out.println((int)(new Date().getTime()/1000));
        //new HttpFileServer().run(8080,"/C:\\Users\\xiongxin\\Desktop");
    }
}
