package JsonServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import net.sf.json.JSONObject;

import java.util.List;

import static io.netty.util.CharsetUtil.UTF_8;

public class HttpJsonEncoder extends MessageToMessageEncoder<HttpJsonResponse> {



    @Override
    protected void encode(ChannelHandlerContext ctx, HttpJsonResponse response, List<Object> list) throws Exception {
        String jsonStr = JSONObject.fromObject(response.getResult()).toString();
        ByteBuf body = Unpooled.copiedBuffer(jsonStr,UTF_8);
        FullHttpResponse res = response.getHttpResponse();
        if (res == null) {
            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, body);
        } else {
            res = new DefaultFullHttpResponse(response.getHttpResponse()
                    .protocolVersion(), response.getHttpResponse().status(),
                    body);
        }
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/json");
        HttpUtil.setContentLength(res, body.readableBytes());
        list.add(res);
    }
}
