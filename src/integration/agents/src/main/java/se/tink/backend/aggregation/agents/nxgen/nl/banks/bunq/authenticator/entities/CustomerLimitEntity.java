package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerLimitEntity {
    @JsonProperty("limit_monetary_account")
    private Integer limitMonetaryAccount;

    @JsonProperty("limit_card_debit_maestro")
    private Integer limitCardDebitMaestro;

    @JsonProperty("limit_card_debit_mastercard")
    private Integer limitCardDebitMastercard;

    @JsonProperty("limit_card_debit_wildcard")
    private Integer limitCardDebitWildcard;

    @JsonProperty("limit_card_debit_replacement")
    private Integer limitCardDebitReplacement;

    public Integer getLimitMonetaryAccount() {
        return limitMonetaryAccount;
    }

    public Integer getLimitCardDebitMaestro() {
        return limitCardDebitMaestro;
    }

    public Integer getLimitCardDebitMastercard() {
        return limitCardDebitMastercard;
    }

    public Integer getLimitCardDebitWildcard() {
        return limitCardDebitWildcard;
    }

    public Integer getLimitCardDebitReplacement() {
        return limitCardDebitReplacement;
    }
}
