package org.whiteandpaint.rhyus;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.whiteandpaint.rhyus.handler.HTTPFrameHandler;
import org.whiteandpaint.rhyus.handler.WebSocketFrameHandler;
import org.whiteandpaint.rhyus.value.CustomValue;

public class WebSocketServer {

    private final int port;

    private final SslContext sslContext;

    public WebSocketServer(int port, SslContext sslContext) {
        this.port = port;
        this.sslContext = sslContext;
    }

    public static void main(String[] args) throws Exception {
        int port = 10831;
        SslContext sslContext = SslContextProvider.createSslContext();
        new WebSocketServer(port, sslContext).run();
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addFirst(sslContext.newHandler(ch.alloc()));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                            pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
                            pipeline.addLast(new HTTPFrameHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                            pipeline.addLast(new WebSocketFrameHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            CustomValue.init();

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Rhyus server started at ws://0.0.0.0:" + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
