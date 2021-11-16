package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AccountsPageLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("endUserIdentity")
    private GenericLinkEntity endUserIdentity = null;

    @JsonProperty("beneficiaries")
    private GenericLinkEntity beneficiaries = null;
}
