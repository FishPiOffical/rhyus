package org.whiteandpaint.rhyus.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.whiteandpaint.rhyus.processor.AuthProcessor;

import java.util.List;
import java.util.Map;

public class HTTPFrameHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String userName = "";
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            // 获取 URI 和解析查询参数
            String uri = request.uri();
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            Map<String, List<String>> parameters = decoder.parameters();

            // 提取 apiKey 参数
            if (parameters.containsKey("apiKey")) {
                List<String> apiKeys = parameters.get("apiKey");
                if (!apiKeys.isEmpty()) {
                    String apiKey = apiKeys.get(0);
                    userName = AuthProcessor.authenticate(ctx, apiKey);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
            return;
        }

        if (!userName.isEmpty()) {
            ctx.fireChannelRead(msg);
        } else {
            ctx.close();
        }
    }
}
