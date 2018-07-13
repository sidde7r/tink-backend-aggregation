package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandshakeRequest {

    private String handshakePubKey;
    private String cnonce;

    public HandshakeRequest setHandshakePubKey(String handshakePubKey) {
        this.handshakePubKey = handshakePubKey;
        return this;
    }

    public HandshakeRequest setCnonce(String cnonce) {
        this.cnonce = cnonce;
        return this;
    }
}
