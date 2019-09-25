package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsEntity {
    @JsonProperty private String productCode;
    @JsonProperty private String cardName;
    @JsonProperty private String cardNr;
    @JsonProperty private String contractNr;
    @JsonProperty private String cardStatus;
    @JsonProperty private String embossName;
    @JsonProperty private String expire;
    @JsonProperty private String memberNr;
    @JsonProperty private String accountNr;
    @JsonProperty private String accountAvailableAmount;
    @JsonProperty private String authorizationRegionId;
    @JsonProperty private Boolean vbv;
    @JsonProperty private Boolean cardRenewal;
    @JsonProperty private Boolean pinReorderOk;
    @JsonProperty private Boolean modifyLimits;
    @JsonProperty private Boolean modifyRegions;
    @JsonProperty private Boolean movableToAccount;
    @JsonProperty private String type;
    @JsonProperty private Boolean transferable;
    @JsonProperty private String cardShortName;
    @JsonProperty private Boolean paymentFreeMonthsChangeAllowed;
    @JsonProperty private Boolean showLimits;

    @JsonIgnore
    public boolean isNotDebit() {
        return !"DEBIT".equalsIgnoreCase(type);
    }

    @JsonIgnore
    public String getContractNr() {
        return contractNr;
    }

    @JsonIgnore
    public String getProductCode() {
        return productCode;
    }
}
