package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class StoreDerivationCdResponseWrapper {
    private OutputEntity output;

    public OutputEntity getOutput() {
        return output;
    }
}
