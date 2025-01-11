package org.whiteandpaint.rhyus.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.whiteandpaint.rhyus.json.JSONObject;
import org.whiteandpaint.rhyus.value.ThreadPool;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BCProcessor {

    public static String fullOnline = "{}";

    public static void commandResolve(ChannelHandlerContext ctx, String command) {
        try {
            if (command.startsWith("tell")) {
                // 发送文本给指定用户
                ThreadPool.quickMessageThreadPool.submit(() -> {
                    String to = command.split(" ")[1];
                    String content = command.replace("tell " + to + " ", "");
                    System.out.println("tell " + to + ": " + content);
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        String name = entry.getValue().optString("userName");
                        if (to.equals(name)) {
                            sendText(entry.getKey(), content);
                        }
                    }
                });
            } else if (command.startsWith("msg")) {
                // 广播文本：指定发送者先收到消息
                String sender = command.split(" ")[1];
                String content = command.replace("msg " + sender + " ", "");
                ThreadPool.quickMessageThreadPool.submit(() -> {
                    System.out.println("msg " + sender + ": " + content);
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        String name = entry.getValue().optString("userName");
                        if (sender.equals(name)) {
                            sendText(entry.getKey(), content);
                        }
                    }
                });

                ThreadPool.messageThreadPool.submit(() -> {
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        String name = entry.getValue().optString("userName");
                        if (!sender.equals(name)) {
                            sendText(entry.getKey(), content);
                            try {
                                Thread.sleep(10);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
            } else if (command.startsWith("all")) {
                // 广播文本：直接广播，所有人按顺序收到消息
                ThreadPool.messageThreadPool.submit(() -> {
                    String content = command.replace("all ", "");
                    System.out.println("all: " + content);
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        sendText(entry.getKey(), content);
                        try {
                            Thread.sleep(10);
                        } catch (Exception ignored) {
                        }
                    }
                });
            } else if (command.startsWith("slow")) {
                // 广播文本：慢广播，慢速发送，但所有人都能收到
                ThreadPool.slowMessageThreadPool.submit(() -> {
                    String content = command.replace("slow ", "");
                    System.out.println("slow: " + content);
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        sendText(entry.getKey(), content);
                        try {
                            Thread.sleep(100);
                        } catch (Exception ignored) {
                        }
                    }
                });
            } else if (command.equals("online")) {
                ThreadPool.adminMessageThreadPool.submit(() -> {
                    Map<String, JSONObject> tempOnlineUsers = new HashMap<>();
                    for(JSONObject obj : AuthProcessor.onlineUsers.values()) {
                        tempOnlineUsers.put(obj.optString("userName"), obj);
                    }
                    String text = Arrays.toString(tempOnlineUsers.values().toArray());
                    sendText(ctx, text);
                });
            } else if (command.equals("hello")) {
                ThreadPool.adminMessageThreadPool.submit(() -> {
                    sendText(ctx, "hello!");
                });
            } else if (command.startsWith("push")) {
                ThreadPool.adminMessageThreadPool.submit(() -> {
                    String content = command.replace("push ", "");
                    System.out.println("push: " + content);
                    fullOnline = content;
                    sendText(ctx, "OK");
                });
            } else if (command.startsWith("kick")) {
                ThreadPool.adminMessageThreadPool.submit(() -> {
                    String userName = command.replace("kick ", "");
                    System.out.println("kick: " + userName);
                    for (Map.Entry<ChannelHandlerContext, JSONObject> entry : AuthProcessor.onlineUsers.entrySet()) {
                        String name = entry.getValue().optString("userName");
                        if (userName.equals(name)) {
                            entry.getKey().close();
                        }
                    }
                });
            } else if (command.equals("clear")) {
                ThreadPool.adminMessageThreadPool.submit(() -> {
                    Map<String, Long> result = AuthProcessor.clearOutdatedUser();
                    System.out.println("clear: " + result.toString());
                    sendText(ctx, result.toString());
                });
            }
        } catch (Exception ignored) {
        }
    }

    public static Integer getOnlineNumber() {
        return getOnline().size();
    }

    public static Map<String, JSONObject> getOnline() {
        try {
            // 使用 HashMap 去重
            Map<String, JSONObject> filteredOnlineUsers = new HashMap<>();
            for (JSONObject object : AuthProcessor.onlineUsers.values()) {
                String name = object.optString("userName");
                filteredOnlineUsers.put(name, object);
            }

            return filteredOnlineUsers;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void sendText(ChannelHandlerContext ctx, String text) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
    }
}
