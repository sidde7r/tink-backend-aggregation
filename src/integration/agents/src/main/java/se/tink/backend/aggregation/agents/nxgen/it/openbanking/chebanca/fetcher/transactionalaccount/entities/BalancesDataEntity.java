package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesDataEntity {
    @JsonProperty("accountAvailability")
    private AmountEntity accountAvailability;

    @JsonProperty("accountAvailableCredit")
    private AmountEntity accountAvailableCredit;

    @JsonProperty("accountBalance")
    private AmountEntity accountBalance;

    @JsonProperty("availableBalance")
    private AmountEntity availableBalance;

    @JsonProperty("date")
    private String date;

    @JsonProperty("hour")
    private String hour;

    @JsonProperty("isPocketAccount")
    private Boolean isPocketAccount;

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }
}
