package org.whiteandpaint.rhyus.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class BCProcessor {

    public static void commandResolve(String command) {
        if (command.startsWith("tell")) {
            // 发送文本给指定用户
            String to = command.split(" ")[1];
            String content = command.replace("tell " + to + " ", "");
            System.out.println("tell " + to + ": " + content);
            
        } else if (command.startsWith("msg")) {
            // 广播文本：指定发送者先收到消息
            String sender = command.split(" ")[1];
            String content = command.replace("msg " + sender + " ", "");
            System.out.println("msg " + sender + ": " + content);
            
        } else if (command.startsWith("all")) {
            // 广播文本：直接广播，所有人按顺序收到消息
            String content = command.replace("all ", "");
            System.out.println("all: " + content);
            
        } else if (command.startsWith("slow")) {
            // 广播文本：慢广播，慢速发送，但所有人都能收到
            // 广播文本：直接广播，所有人按顺序收到消息
            String content = command.replace("slow ", "");
            System.out.println("slow: " + content);

        }
    }

    public static void sendText(ChannelHandlerContext ctx, String text) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
    }
}
