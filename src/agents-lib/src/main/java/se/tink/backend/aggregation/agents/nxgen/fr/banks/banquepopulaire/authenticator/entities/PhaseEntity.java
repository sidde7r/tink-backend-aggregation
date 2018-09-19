package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PhaseEntity {
    private String state;
    private int retryCounter;
    private boolean fallbackFactorAvailable;
    private String securityLevel;
    private String previousResult;

    public String getPreviousResult() {
        return previousResult;
    }
}
