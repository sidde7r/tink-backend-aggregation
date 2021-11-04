package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TermsAndConditionsEntity {
    private String changeType;
    private String renewalCanMakeChangesDate;
    private String renewalTakesEffectDate;
    private LinkEntity links;
}
