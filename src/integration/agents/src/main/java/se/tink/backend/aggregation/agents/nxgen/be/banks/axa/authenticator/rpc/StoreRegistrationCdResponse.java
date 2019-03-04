package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.StoreDerivationCdResponseWrapper;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class StoreRegistrationCdResponse {
    @JsonProperty("StoreDerivationCdResponseWrapper")
    private StoreDerivationCdResponseWrapper storeDerivationCdResponseWrapper;

    public boolean wasSuccessful() {
        if (storeDerivationCdResponseWrapper == null) {
            return false;
        }
        if (storeDerivationCdResponseWrapper.getOutput() == null) {
            return false;
        }
        return storeDerivationCdResponseWrapper.getOutput().getErrors() == null;
    }
}
