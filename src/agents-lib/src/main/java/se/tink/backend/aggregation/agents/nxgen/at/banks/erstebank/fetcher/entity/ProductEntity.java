package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String id;
    private String type;
    private String identifier;
    private String description;
    private String title;
    @JsonProperty("amount")
    private AmountEntity amountEntity;
    @JsonProperty("accountInfo")
    private AccountInfoEntity accountInfoEntity;
    @JsonProperty("extrasInfo")
    private ExtraInfoEntity extraInfoEntity;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    public AccountInfoEntity getAccountInfoEntity() {
        return accountInfoEntity;
    }

    public ExtraInfoEntity getExtraInfoEntity() {
        return extraInfoEntity;
    }
}
