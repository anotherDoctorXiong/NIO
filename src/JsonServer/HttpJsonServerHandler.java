package JsonServer;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;


public class HttpJsonServerHandler extends SimpleChannelInboundHandler<HttpJsonRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpJsonRequest request) throws Exception {

        if(request.getRequest().getMethod()== HttpMethod.POST){
            FullHttpResponse res=new DefaultFullHttpResponse
                    (HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer("i get it",CharsetUtil.UTF_8));
            res.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;chatset=UTF-8");
            ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
            ctx.writeAndFlush(new HttpJsonResponse(null, request.getBody()));
        }else{

            Map add = new HashMap<String, String>() {{
                put("faceId", "id");
                put("username", "name");
                put("valid", "9999");
                put("wgId", "");
            }};
            ctx.writeAndFlush(new HttpJsonResponse(null,add)).addListener(ChannelFutureListener.CLOSE);
        }



    }

}
