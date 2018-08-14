package io.github.hologuide.hlgservice;

import android.util.Log;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

import java.nio.charset.StandardCharsets;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter
{
    private static final String APPNAME = "HTTPServerHandler";

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {

        if (msg instanceof HttpRequest)
        {
            HttpRequest httpRequest = (HttpRequest) msg;

            Log.d(APPNAME, "Http Request Received");

            HttpHeaders headers = httpRequest.headers();
            Log.d(APPNAME, "Connection : " +headers.get("Connection"));
            Log.d(APPNAME, "Upgrade : " + headers.get("Upgrade"));

            Log.d(APPNAME, "[" + httpRequest.uri() + "]");

            // WebSocketの通信 かつ /wsへのアクセスなら
            if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                    "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE)) &&
                    "/ws".equalsIgnoreCase(httpRequest.uri()) )
            {
                // WebSocket
                Log.d(APPNAME, "Adding new handler...");

                ctx.pipeline().replace(this, "WebSocketHandler", new WebSocketHandler());
                ctx.fireChannelActive(); // WebSocketHandlerのchannelActive()を発火

                Log.d(APPNAME, "WebSocketHandler added to the pipeline");
                Log.d(APPNAME, "Opened Channel : " + ctx.channel());
                Log.d(APPNAME, "Handshaking....");

                // ハンドシェイク
                handleHandshake(ctx, httpRequest);
                Log.d(APPNAME, "Handshake is done");
            }
            else
            {
                // HTTP

                final String html = "<!DOCTYPE html><html lang=\"ja\"><head><meta charset=\"UTF-8\"><title>hlgService</title></head><body><h1>hlgService (HoloGuide) is successfully working.</h1></body></html>";
                byte[] content = html.getBytes(StandardCharsets.UTF_8);

                DefaultHttpResponse defaultHttpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.buffer().writeBytes(content),
                        new DefaultHttpHeaders()
                                .add(HttpHeaderNames.CONTENT_TYPE, "text/html")
                                .add(HttpHeaderNames.CONTENT_LENGTH, content.length),
                        EmptyHttpHeaders.INSTANCE
                );

                if (!HttpUtil.isKeepAlive(httpRequest))
                {
                    ctx.writeAndFlush(defaultHttpResponse)
                            .addListener(ChannelFutureListener.CLOSE);
                }
                else
                {
                    defaultHttpResponse.headers()
                            .add(HttpHeaderNames.CONNECTION, KEEP_ALIVE);
                    ctx.writeAndFlush(defaultHttpResponse);
                }

            }

        }
        else
        {
            Log.d(APPNAME, "Incoming request is unknown");
        }

    }

    /* Do the handshaking for WebSocket request */
    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req)
    {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        handshaker = wsFactory.newHandshaker(req);

        if (handshaker == null)
        {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else
        {
            handshaker.handshake(ctx.channel(), req);
        }

    }

    protected String getWebSocketURL(HttpRequest req)
    {
        Log.d(APPNAME, "Req URI : " + req.uri());
        String url =  "ws://" + req.headers().get("Host") + req.uri();
        Log.d(APPNAME, "URL : " + url);

        return url;
    }
}