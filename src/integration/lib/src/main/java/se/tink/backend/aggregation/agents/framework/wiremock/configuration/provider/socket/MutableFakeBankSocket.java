package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket;

import com.google.common.base.Preconditions;

public class MutableFakeBankSocket implements FakeBankSocket {

    private String httpHost = null;
    private String httpsHost = null;

    // Hidden in order to prevent Guice from creating instances invisibly
    private MutableFakeBankSocket() {}

    public static MutableFakeBankSocket create() {
        return new MutableFakeBankSocket();
    }

    public static MutableFakeBankSocket of(final String httpHost, final String httpsHost) {
        final MutableFakeBankSocket socket = new MutableFakeBankSocket();
        socket.httpHost = httpHost;
        socket.httpsHost = httpsHost;
        return socket;
    }

    public void clear() {
        this.httpHost = null;
        this.httpsHost = null;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public void setHttpsHost(String httpsHost) {
        this.httpsHost = httpsHost;
    }

    @Override
    public String getHttpHost() {
        return Preconditions.checkNotNull(httpHost, "HTTP host needs to be set");
    }

    @Override
    public String getHttpsHost() {
        return Preconditions.checkNotNull(httpsHost, "HTTPS host needs to be set");
    }
}
