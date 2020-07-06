package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Phase {

    @JsonProperty("retryCounter")
    private int retryCounter;

    @JsonProperty("securityLevel")
    private String securityLevel;

    @JsonProperty("fallbackFactorAvailable")
    private boolean fallbackFactorAvailable;

    @JsonProperty("state")
    private String state;

    @JsonProperty("previousResult")
    private String previousResult;
}
