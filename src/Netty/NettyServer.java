package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //HttpServerCodec: 针对http协议进行编解码
                            pipeline.addLast("httpServerCodec", new HttpServerCodec());
                            //ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
                            pipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
                            /**
                             * 作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse，那么具体的是什么
                             * 取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
                             */
                            pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));

                            //用于处理websocket, /ws为访问websocket时的uri
                            pipeline.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws"));

                            pipeline.addLast("myWebSocketHandler", new WebSocketHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(8989).sync();
            channelFuture.channel().closeFuture().sync();
        }finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
