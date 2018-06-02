package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.CreditCardAccountEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountsListResponse {
    private List<CreditCardAccountEntity> creditCards;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
    @JsonProperty("_aggregatedAmountFraction")
    private String aggregatedAmountFraction;
    @JsonProperty("_aggregatedAmountInteger")
    private String aggregatedAmountInteger;

    public List<CreditCardAccountEntity> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<CreditCardAccountEntity> creditCards) {
        this.creditCards = creditCards;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }

    public String getAggregatedAmountFraction() {
        return aggregatedAmountFraction;
    }

    public void setAggregatedAmountFraction(String aggregatedAmountFraction) {
        this.aggregatedAmountFraction = aggregatedAmountFraction;
    }

    public String getAggregatedAmountInteger() {
        return aggregatedAmountInteger;
    }

    public void setAggregatedAmountInteger(String aggregatedAmountInteger) {
        this.aggregatedAmountInteger = aggregatedAmountInteger;
    }
}
