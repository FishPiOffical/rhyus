package org.whiteandpaint.rhyus.processor;

import io.netty.channel.ChannelHandlerContext;
import org.whiteandpaint.rhyus.json.JSONObject;
import org.whiteandpaint.rhyus.value.Config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AuthProcessor {

    public static final Set<ChannelHandlerContext> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    public static final ConcurrentHashMap<ChannelHandlerContext, JSONObject> onlineUsers = new ConcurrentHashMap<>();

    public static String authenticate(ChannelHandlerContext ctx, String apiKey) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

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
                return userName;
            }
        } catch (Exception e) {
            ctx.close();
        }
        ctx.close();
        return "";
    }
}
