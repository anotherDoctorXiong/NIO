package FileServer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

public class FilesServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
    public FilesServerHandler(String url) {
        this.url = url;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(!request.decoderResult().isSuccess()){
            sendMessage(ctx,HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if(request.method()!=HttpMethod.GET){
            sendMessage(ctx,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        String uri=request.uri();
        String path=uri;

        if(path==null){
            sendMessage(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }
        File file=new File(path);
        if(file.isHidden()||!file.exists()){
            sendMessage(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }
        if(file.isDirectory()){
            if(uri.endsWith("/")){
                sendListing(ctx,file);
            }else {
                sendRedirect(ctx,uri+"/");
            }
            return;
        }
        if(!file.isFile()){
            sendMessage(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }
        RandomAccessFile randomAccessFile=null;

        try {
            randomAccessFile=new RandomAccessFile(file,"r");
        } catch (FileNotFoundException e) {
            sendMessage(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }
        long filelength=randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        //设置Content Length
        HttpUtil.setContentLength(response, filelength);
        //设置Content Type

        response.headers().set(HttpHeaderNames.CONTENT_TYPE,file.getPath());
        //如果request中有KEEP ALIVE信息
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ChannelFuture sendFuture;
        sendFuture=ctx.write(new ChunkedFile(randomAccessFile,0,filelength,8192),ctx.newProgressivePromise());
        sendFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future,
                                            long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.err.println("Transfer progress: " + progress + " / "
                            + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("Transfer complete.");
            }
        });
        ChannelFuture lastContentFuture = ctx
                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //如果不支持keep-Alive，服务器端主动关闭请求
        if (!HttpUtil.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }
    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    private static void sendMessage(ChannelHandlerContext ctx, HttpResponseStatus status){
        FullHttpResponse res=new DefaultFullHttpResponse
                (HttpVersion.HTTP_1_1,status, Unpooled.copiedBuffer
                        ("failure:"+status.toString()+"\r\n", CharsetUtil.UTF_8));
        res.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;chatset=UTF-8");
        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }
    private String filterUri(String uri){
        System.out.println(uri);
        /*try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        uri = uri.replace('/', File.separatorChar);
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }*/
        return System.getProperty("user.dir") + File.separator + uri;
    }
    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
