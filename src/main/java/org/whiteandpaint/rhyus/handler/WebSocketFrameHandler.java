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
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
        super.channelActive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String userName = "";
        for (ChannelHandlerContext key : AuthProcessor.onlineUsers.keySet()) {
            if (key.channel().id().asLongText().equals(ctx.channel().id().asLongText())) {
                userName = AuthProcessor.onlineUsers.get(key).optString("userName");
            }
        }
        AuthProcessor.onlineUsers.entrySet().removeIf(entry -> entry.getKey().channel().id().asLongText().equals(ctx.channel().id().asLongText()));
        boolean leave = true;
        for (ChannelHandlerContext key : AuthProcessor.onlineUsers.keySet()) {
            if (AuthProcessor.onlineUsers.get(key).optString("userName").equals(userName) && !userName.isEmpty()) {
                leave = false;
            }
        }
        if (leave && !userName.isEmpty()) {
            System.out.println(userName + " has left. SESSIONS=" + AuthProcessor.onlineUsers.size());
            AuthProcessor.postMessageToMaster(Config.adminKey, "leave", userName);
        }
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
