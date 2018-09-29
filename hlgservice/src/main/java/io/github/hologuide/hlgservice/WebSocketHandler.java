package io.github.hologuide.hlgservice;

import android.util.Log;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;

import com.annimon.stream.*;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{
    private static final String APPNAME = "WebSocketHandler";

    public WebSocketHandler()
    {
        super(false);
    }

    private static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws  Exception
    {
        if (frame instanceof TextWebSocketFrame)
        {
            final String text = ((TextWebSocketFrame)frame).text();
            Log.d(APPNAME, "Received text frame : " + text);

            ServiceManager.OnReceived(text);

            PromiseCombiner promiseCombiner = new PromiseCombiner();

            // allChannels.stream()
            Stream.of(allChannels)
                    .filter(c -> c != ctx.channel())
                    .forEach(c -> {
                        frame.retain();
                        promiseCombiner.add(c.writeAndFlush(frame.duplicate()).addListener((ChannelFutureListener) channelFuture -> {
                            if (!channelFuture.isSuccess())
                            {
                                Log.d(APPNAME, "Failed to write to channel : " + channelFuture.cause());
                            }
                        }));
                    });

            Promise aggPromise = ctx.newPromise();
            promiseCombiner.finish(aggPromise);

            aggPromise.addListener((ChannelFutureListener) channelFuture ->
            {
                if (frame.release())
                {
                    Log.d(APPNAME, "WebSocket frame successfully deallocated.");
                } else
                {
                    Log.d(APPNAME, "WebSocket frame leaked!");
                }
            });

        } else
        {
            throw new UnsupportedOperationException("Invalid WebSocket frame received.");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // Disconnected.
        ServiceManager.OnDisconnected(ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // Connected.
        Log.d(APPNAME, "Adding new channel " + ctx.channel().remoteAddress() + "to list of channels");
        allChannels.add(ctx.channel());

        ServiceManager.OnConnected(ctx.channel().remoteAddress().toString());
    }

    public static void sendBroadcast(String text)
    {
        if (!allChannels.isEmpty())
        {
            // allChannels.stream()
            Stream.of(allChannels)
                .forEach(c ->
                    c.writeAndFlush(new TextWebSocketFrame(text))
                );
        }

    }

}
