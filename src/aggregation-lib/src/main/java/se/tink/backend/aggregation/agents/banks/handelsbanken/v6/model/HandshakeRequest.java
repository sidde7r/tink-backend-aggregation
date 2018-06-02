package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class HandshakeRequest {
    private String handshakePubKey;
    private String cnonce;

    public HandshakeRequest(String cnonce, String handshakePubKey) {
        this.cnonce = cnonce;
        this.handshakePubKey = handshakePubKey;
    }

    public String getHandshakePubKey() {
        return handshakePubKey;
    }

    public void setHandshakePubKey(String handshakePubKey) {
        this.handshakePubKey = handshakePubKey;
    }

    public String getCnonce() {
        return cnonce;
    }

    public void setCnonce(String cnonce) {
        this.cnonce = cnonce;
    }

}
