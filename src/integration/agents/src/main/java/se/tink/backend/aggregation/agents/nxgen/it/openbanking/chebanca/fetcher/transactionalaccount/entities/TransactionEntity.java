package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    @JsonProperty("amountTransaction")
    private AmountEntity amountTransaction;

    @JsonProperty("extendedDescription")
    private String extendedDescription;

    @JsonProperty("dateAccountingCurrency")
    private String dateAccountingCurrency;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("dateLiquidationValue")
    private String dateLiquidationValue;

    @JsonProperty("codeDescription")
    private String codeDescription;

    public AmountEntity getAmountTransaction() {
        return amountTransaction;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public String getDateAccountingCurrency() {
        return dateAccountingCurrency;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDateLiquidationValue() {
        return dateLiquidationValue;
    }

    public String getCodeDescription() {
        return codeDescription;
    }
}
