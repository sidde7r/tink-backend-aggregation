package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksBalancesEntity {

    private HrefEntity self;

    private HrefEntity transactions;

    @JsonProperty("parent-list")
    private HrefEntity parentList;
}
