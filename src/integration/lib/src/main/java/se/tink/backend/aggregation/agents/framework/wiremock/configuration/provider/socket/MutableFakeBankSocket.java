package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket;

import com.google.common.base.Preconditions;

public class MutableFakeBankSocket implements FakeBankSocket {

    private String socketAddress = null;

    // Hidden in order to prevent Guice from creating instances invisibly
    private MutableFakeBankSocket() {}

    public static MutableFakeBankSocket create() {
        return new MutableFakeBankSocket();
    }

    public static MutableFakeBankSocket of(final String socketAddress) {
        final MutableFakeBankSocket socket = new MutableFakeBankSocket();
        socket.socketAddress = socketAddress;
        return socket;
    }

    public void set(final String socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public String get() {
        return Preconditions.checkNotNull(socketAddress, "Socket needs to be set");
    }
}
