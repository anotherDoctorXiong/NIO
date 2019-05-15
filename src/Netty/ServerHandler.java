package Netty;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter{

   public void channelRead(ChannelHandlerContext ctx,Object msg){
       ByteBuf in =(ByteBuf) msg;
       System.out.println(
               "Received:"+in.toString()
       );
       ctx.writeAndFlush(in).addListener(ChannelFutureListener.CLOSE);
   }
   public void channelReadComplete(ChannelHandlerContext ctx){
       System.out.println("complete");
       ctx.writeAndFlush(Unpooled.copiedBuffer("Complete", CharsetUtil.UTF_8));
   }
   public void exCaught(ChannelHandlerContext ctx,Throwable cause){
       cause.printStackTrace();
       ctx.close();
   }
}
