package Netty;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;


import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;


public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private String url;
    private static File index;
    static {
        try{
            URL url1=HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
            String path=url1.toURI()+"index.html";
            path=!path.contains("file:")?path:path.substring(5);
            index=new File(path);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public HttpRequestHandler(String url) {
        this.url = url;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        if (url.equalsIgnoreCase(msg.uri())) {
            ctx.fireChannelRead(msg.retain());
        }else {

            if(HttpHeaders.is100ContinueExpected(msg)){
                System.out.println("不符合1.1");
                send100Continue(ctx);
            }
            RandomAccessFile file=new RandomAccessFile(index,"r");
            HttpResponse res=new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html; charset=UTF-8");
            boolean keep=HttpHeaders.isKeepAlive(msg);
            if(keep){
                res.headers().set(HttpHeaderNames.CONTENT_LENGTH,String.valueOf(file.length()));
                res.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(res);
            if(ctx.pipeline().get(SslHandler.class)==null){
                ctx.write(new DefaultFileRegion(file.getChannel(),0,file.length()));
            }else{
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            ChannelFuture f=ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if(!keep){
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }




    private static void send100Continue(ChannelHandlerContext ctx){
        FullHttpResponse res=new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE
        );
        ctx.writeAndFlush(res);
    }


}
