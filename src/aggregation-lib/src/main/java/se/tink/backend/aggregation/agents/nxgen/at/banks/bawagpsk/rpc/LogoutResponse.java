package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;

public class LogoutResponse {
    private Envelope envelope;

    public LogoutResponse(final Envelope envelope) {
        this.envelope = envelope;
    }
}
