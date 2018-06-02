package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class HandshakeContent {
    private HandshakeServerHello serverHello;

    public HandshakeServerHello getServerHello() {
        return serverHello;
    }

    public void setServerHello(HandshakeServerHello serverHello) {
        this.serverHello = serverHello;
    }
}
