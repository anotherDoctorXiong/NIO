package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

public class server {


    public static void main(String[] args)throws Exception {
        server s=new server();
        s.start(new InetSocketAddress(9955));
    }
    public void start(InetSocketAddress address)throws Exception{
        System.out.println("server start");


        ChannelGroup channelgroup=new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

        EventLoopGroup group =new NioEventLoopGroup();
        try{
            ServerBootstrap b=new ServerBootstrap();
            //Bootstrap绑定EventLoopGroup
            b.group(group)
                    //指定阻塞模式
                    .channel(NioServerSocketChannel.class)
                    //指定套接字地址
                    .localAddress(address)
                    //将ServerHandler添加到子Channel的channelpipeline中
                    .childHandler(new ServerHandler());
            //异步绑定服务器，阻塞知道完成绑定
            ChannelFuture f=b.bind().sync();
            //获取CloseFuture阻塞它完成
            f.channel().closeFuture().sync();
        }finally {
            //释放资源
            channelgroup.close();
            group.shutdownGracefully().sync();
        }
    }
    protected ChannelInitializer<Channel> MyInitializer(ChannelGroup channelGroup){
        return new HttpPipeline(channelGroup);
    }
}
