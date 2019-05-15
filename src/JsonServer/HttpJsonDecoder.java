package JsonServer;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpJsonDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> list) throws Exception {
        if(request.decoderResult().isSuccess()){
            Map<String, Object> params=getFormParams(request);
            params.forEach((key, value) -> {
                System.out.println(key + ":" + value);
            });

            String content=request.content().toString(CharsetUtil.UTF_8);

            HttpJsonRequest req=new HttpJsonRequest(request,content);
            list.add(req);
        }
    }
    private  Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<>();
        if(fullHttpRequest.method()== HttpMethod.GET){
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        }else if(fullHttpRequest.method()== HttpMethod.POST){
            String content_type=fullHttpRequest.headers().get("Content-Type").trim();
            if(content_type.contains("form-data")||content_type.contains("www-form-urlencoded")){
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
                List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
                for (InterfaceHttpData data : postData) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        MemoryAttribute attribute = (MemoryAttribute) data;
                        params.put(attribute.getName(), attribute.getValue());
                    }
                }
                return params;
            }else if(content_type.contains("application/json")){
                String content=fullHttpRequest.content().toString(CharsetUtil.UTF_8);
                params=JSONObject.fromObject(content);
                return params;
            }else{
                params=null;
                return params;
            }
        }else {
            params=null;
            return params;
        }
    }
}
