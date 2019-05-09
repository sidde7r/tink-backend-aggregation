package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequestBody {

    private AccessEntity access;
    private Boolean combinedServiceIndicator;
    private Integer frequencyPerDay;
    private Boolean recurringIndicator;
    private String validUntil;

    public ConsentRequestBody(String validDate, Integer frequencyPerDay) {
        this.access = new AccessEntity();
        this.recurringIndicator = false;
        this.validUntil = validDate;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = false;
    }

    public AccessEntity getAccess() {
        return access;
    }

    public void setAccess(AccessEntity access) {
        this.access = access;
    }
}
