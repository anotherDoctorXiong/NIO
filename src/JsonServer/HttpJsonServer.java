package JsonServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

public class HttpJsonServer {
    public void run(final int port) throws Exception {
        EventLoopGroup group=new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            //接收HttpJsonRequest，需要对应解码器
                            //ByteBuf->FullHttpRequest-> HttpJsonRequestDecoder
                            //输出HttpJsonResponse，需要对应编码器
                            //HttpResponseEncoder->FullHttpResponse-> HttpJsonResponseEncoder
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("json-decoder", new HttpJsonDecoder());
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            ch.pipeline().addLast("json-encoder", new HttpJsonEncoder());
                            ch.pipeline().addLast("jsonServerHandler", new HttpJsonServerHandler());
                        }
                    });
            ChannelFuture future = b.bind(new InetSocketAddress(port)).sync();
            System.out.println("HTTP订购服务器启动，网址是 : " + "http://localhost:"
                    + port);
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args)throws Exception {
        new HttpJsonServer().run(8080);
    }
}
