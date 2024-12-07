package org.whiteandpaint.rhyus.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.whiteandpaint.rhyus.processor.AuthProcessor;
import org.whiteandpaint.rhyus.processor.BCProcessor;
import org.whiteandpaint.rhyus.value.Config;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(BCProcessor.fullOnline));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        AuthProcessor.onlineUsers.entrySet().removeIf(entry -> entry.getKey().channel().id().asLongText().equals(ctx.channel().id().asLongText()));
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();
        if (text.contains(":::")) {
            String[] parts = text.split(":::");
            if (parts.length == 2) {
                if (parts[0].equals(Config.adminKey)) {
                    String command = parts[1];
                    BCProcessor.commandResolve(channelHandlerContext, command);
                }
            }
        }
    }
}
