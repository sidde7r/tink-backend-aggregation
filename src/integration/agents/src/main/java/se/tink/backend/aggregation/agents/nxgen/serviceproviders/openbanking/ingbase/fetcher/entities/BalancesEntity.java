package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date referenceDate;

    @JsonIgnore
    public boolean isExpected() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.EXPECTED);
    }

    @JsonIgnore
    public boolean isInterimBooked() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.INTERIM_BOOKED);
    }

    @JsonIgnore
    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.CLOSING_BOOKED);
    }

    @JsonIgnore
    public String getCurrency() {
        return balanceAmount.getCurrency();
    }

    @JsonIgnore
    public ExactCurrencyAmount getAmount() {
        return balanceAmount.toAmount();
    }
}
