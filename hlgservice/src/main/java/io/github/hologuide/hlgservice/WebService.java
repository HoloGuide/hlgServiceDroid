package io.github.hologuide.hlgservice;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WebService extends IntentService
{
    public static volatile boolean ShouldContinue = true;

    private static final int PORT = 8080;
    private static final String APPNAME = "WebService";

    private static GPSLocationListener m_GPSLocationListener = new GPSLocationListener();

    public WebService(String name)
    {
        super(name);
    }

    public WebService()
    {
        super("WebService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(APPNAME,"onHandleIntent Start");

        ShouldContinue = true;

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer());

            Channel ch = b.bind(PORT).sync().channel();
            Log.d(APPNAME, "Server Launched.");

            while(!ch.closeFuture().isDone())
            {
                synchronized (this)
                {
                    if (!ShouldContinue)
                    {
                        stopSelf();
                        return;
                    }
                }

            }
            Log.d(APPNAME, "Server Stopped.");

        } catch (InterruptedException e)
        {
            e.printStackTrace();

        } finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}