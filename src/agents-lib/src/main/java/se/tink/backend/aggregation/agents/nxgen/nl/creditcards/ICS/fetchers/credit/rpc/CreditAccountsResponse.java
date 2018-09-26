package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.MetaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditAccountsResponse {
    @JsonProperty("Data")
    private AccountDataEntity data;
    @JsonProperty("Links")
    private LinksEntity links;
    @JsonProperty("Meta")
    private MetaEntity meta;

    public AccountDataEntity getData() {
        return data;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public MetaEntity getMeta() {
        return meta;
    }
}
