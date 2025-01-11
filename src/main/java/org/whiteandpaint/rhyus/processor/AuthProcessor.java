package org.whiteandpaint.rhyus.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.whiteandpaint.rhyus.json.JSONObject;
import org.whiteandpaint.rhyus.value.Config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthProcessor {

    public static final ConcurrentHashMap<ChannelHandlerContext, JSONObject> onlineUsers = new ConcurrentHashMap<>();

    public static final Map<String, Long> userActive = Collections.synchronizedMap(new HashMap<>());

    public static String authenticate(ChannelHandlerContext ctx, String apiKey) {
        if (apiKey.equals(Config.adminKey)) {
            return "admin";
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.remoteServer + "/api/user?apiKey=" + apiKey))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            if (json.optString("code").equals("0")) {
                JSONObject data = json.optJSONObject("data");
                String userName = data.optString("userName");
                userActive.put(userName, System.currentTimeMillis());
                boolean join = true;
                int count = 0;
                for (ChannelHandlerContext key : onlineUsers.keySet()) {
                    if (onlineUsers.get(key).optString("userName").equals(userName)) {
                        join = false;
                        count++;
                    }
                }
                if (count > 10) {
                    return "";
                }
                onlineUsers.put(ctx, data);
                if (join) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                System.out.println(userName + " has joined. SESSIONS=" + AuthProcessor.onlineUsers.size());
                                AuthProcessor.postMessageToMaster(Config.adminKey, "join", userName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                return userName;
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public static void postMessageToMaster(String adminKey, String msg, String data) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", msg);
            jsonObject.put("data", data);
            jsonObject.put("adminKey", adminKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.remoteServer + "/chat-room/node/push"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
        }
    }

    public static Map<String, Long> clearOutdatedUser() {
        Map<String, JSONObject> filteredOnlineUsers = new HashMap<>();
        for (JSONObject object : onlineUsers.values()) {
            String name = object.optString("userName");
            filteredOnlineUsers.put(name, object);
        }
        Long currentTime = System.currentTimeMillis();
        int sixHours = 1000 * 60 * 60 * 6;
        Map<String, Long> needKickUsers = new HashMap<>();
        List<String> users = new ArrayList<>();
        for (String user : filteredOnlineUsers.keySet()) {
            try {
                Long activeTime = userActive.get(user);
                Long spareTime = currentTime - activeTime;
                if (spareTime >= sixHours) {
                    needKickUsers.put(user, (spareTime / (1000 * 60 * 60)));
                    users.add(user);
                }
            } catch (Exception ignored) {
            }
        }

        List<ChannelHandlerContext> senderSessions = new ArrayList<>();
        for (Map.Entry<ChannelHandlerContext, JSONObject> entry : onlineUsers.entrySet()) {
            try {
                String tempUserName = entry.getValue().optString("userName");
                if (users.contains(tempUserName)) {
                    senderSessions.add(entry.getKey());
                }
            } catch (Exception ignored) {
            }
        }
        for (ChannelHandlerContext ctx : senderSessions) {
            try {
                ctx.close();
            } catch (Exception ignored) {
            }
        }

        return needKickUsers;
    }
}
