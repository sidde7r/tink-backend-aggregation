package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class HandshakeResponse extends AbstractResponse {
    private String serverHello;

    public String getServerHello() {
        return serverHello;
    }

    public void setServerHello(String serverHello) {
        this.serverHello = serverHello;
    }
}
