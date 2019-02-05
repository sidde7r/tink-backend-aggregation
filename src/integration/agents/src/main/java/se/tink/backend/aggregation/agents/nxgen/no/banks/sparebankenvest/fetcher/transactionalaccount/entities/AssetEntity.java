package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetEntity {
    @JsonProperty("avtale")
    private Boolean agreement;
    @JsonProperty("betaling")
    private Boolean payment;
    @JsonProperty("tilgang")
    private Boolean asset;
    @JsonProperty("overfoereFra")
    private Boolean transferFrom;
    @JsonProperty("overfoereTil")
    private Boolean transferTo;

    public Boolean getAgreement() {
        return agreement;
    }

    public Boolean getPayment() {
        return payment;
    }

    public Boolean getAsset() {
        return asset;
    }

    public Boolean getTransferFrom() {
        return transferFrom;
    }

    public Boolean getTransferTo() {
        return transferTo;
    }
}
