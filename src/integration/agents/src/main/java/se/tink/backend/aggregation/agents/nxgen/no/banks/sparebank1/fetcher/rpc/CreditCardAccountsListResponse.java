package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardAccountsListResponse {
    private List<CreditCardAccountEntity> creditCards;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonProperty("_aggregatedAmountFraction")
    private String aggregatedAmountFraction;

    @JsonProperty("_aggregatedAmountInteger")
    private String aggregatedAmountInteger;

    public List<CreditCardAccountEntity> getCreditCards() {
        return creditCards == null ? Collections.emptyList() : creditCards;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public String getAggregatedAmountFraction() {
        return aggregatedAmountFraction;
    }

    public String getAggregatedAmountInteger() {
        return aggregatedAmountInteger;
    }
}
