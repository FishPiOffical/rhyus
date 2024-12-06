package org.whiteandpaint.rhyus.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class BCProcessor {

    public static void commandResolve(String command) {

    }

    public static void sendText(ChannelHandlerContext ctx, String text) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
    }
}
