package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils.srp;

import java.math.BigInteger;

public class ClientEvidenceMessageResponse {
    private final BigInteger clientPublicValue; // A
    private final BigInteger clientEvidenceMessage; // M1
    private final BigInteger sessionKey; // S

    public ClientEvidenceMessageResponse(
            BigInteger clientPublicValue, BigInteger clientEvidenceMessage, BigInteger sessionKey) {
        this.clientPublicValue = clientPublicValue;
        this.clientEvidenceMessage = clientEvidenceMessage;
        this.sessionKey = sessionKey;
    }

    public BigInteger getClientPublicValueAsBigInteger() {
        return clientPublicValue;
    }

    public BigInteger getClientEvidenceMessageAsBigInteger() {
        return clientEvidenceMessage;
    }

    public BigInteger getSessionKeyAsBigInteger() {
        return sessionKey;
    }

    public byte[] getClientPublicValueAsBytes() {
        return clientPublicValue.toByteArray();
    }

    public byte[] getClientEvidenceMessageAsBytes() {
        return clientEvidenceMessage.toByteArray();
    }

    public byte[] getSessionKeyAsBytes() {
        return sessionKey.toByteArray();
    }
}
