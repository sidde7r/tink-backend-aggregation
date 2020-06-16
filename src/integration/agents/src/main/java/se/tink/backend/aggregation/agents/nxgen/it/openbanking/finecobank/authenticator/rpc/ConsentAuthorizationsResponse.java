package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAuthorizationsResponse {

    private AccessItem access;
    private String consentStatus;
    private String validUntil;
    private String recurringIndicator;
    private String lastActionDate;
    private String frequencyPerDay;

    public AccessItem getAccess() {
        return access;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public String getRecurringIndicator() {
        return recurringIndicator;
    }

    public String getLastActionDate() {
        return lastActionDate;
    }

    public String getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setAccess(AccessItem access) {
        this.access = access;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }
}
