package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesDataEntity {
    private AmountEntity accountAvailability;
    private AmountEntity accountAvailableCredit;
    private AmountEntity accountBalance;
    private AmountEntity availableBalance;
    private String date;
    private String hour;
    private Boolean isPocketAccount;

    @JsonIgnore
    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }
}
