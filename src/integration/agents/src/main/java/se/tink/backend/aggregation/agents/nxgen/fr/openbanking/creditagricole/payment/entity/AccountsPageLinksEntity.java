package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsPageLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("endUserIdentity")
    private GenericLinkEntity endUserIdentity = null;

    @JsonProperty("beneficiaries")
    private GenericLinkEntity beneficiaries = null;

    public GenericLinkEntity getSelf() {
        return self;
    }

    public GenericLinkEntity getEndUserIdentity() {
        return endUserIdentity;
    }

    public GenericLinkEntity getBeneficiaries() {
        return beneficiaries;
    }
}
