package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;

public class ServiceResponse {
    private Envelope envelope;

    public ServiceResponse(final Envelope envelope) {
        this.envelope = envelope;
    }

    public boolean requestWasSuccessful() {
        return Optional.ofNullable(envelope.getBody().getServiceResponseEntity().getActionCall()).isPresent();
    }
}
