package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TermsAcceptanceInfo {
    private boolean mustAcceptTerms;

    public boolean isMustAcceptTerms() {
        return mustAcceptTerms;
    }
}
