package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesLinkEntity {
    private String href;

    @JsonIgnore
    public String getLink() {
        return href;
    }
}
