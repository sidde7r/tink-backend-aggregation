package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TermsAndConditionsEntity {
    private String changeType;
    private String renewalCanMakeChangesDate;
    private String renewalTakesEffectDate;
    private LinkEntity links;

    public String getChangeType() {
        return changeType;
    }

    public String getRenewalCanMakeChangesDate() {
        return renewalCanMakeChangesDate;
    }

    public String getRenewalTakesEffectDate() {
        return renewalTakesEffectDate;
    }

    public LinkEntity getLinks() {
        return links;
    }
}
