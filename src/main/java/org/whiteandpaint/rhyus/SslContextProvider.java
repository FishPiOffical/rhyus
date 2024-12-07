package org.whiteandpaint.rhyus;

import io.netty.handler.ssl.*;
import javax.net.ssl.SSLException;
import java.io.File;

public class SslContextProvider {
    public static SslContext createSslContext() throws SSLException {
        return SslContext.newServerContext(new File("sslCert/cert.pem"), new File("sslCert/key.pem"));
    }
}
