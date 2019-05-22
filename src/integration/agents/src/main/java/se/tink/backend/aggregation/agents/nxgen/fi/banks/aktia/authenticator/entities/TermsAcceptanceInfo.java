package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TermsAcceptanceInfo {

    @JsonProperty("mustAcceptTerms")
    private boolean mustAcceptTerms;
}
