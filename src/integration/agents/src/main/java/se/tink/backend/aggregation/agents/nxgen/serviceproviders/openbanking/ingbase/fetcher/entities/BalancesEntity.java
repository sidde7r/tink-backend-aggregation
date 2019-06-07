package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date referenceDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date lastChangeDateTime;

    public boolean isExpected() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.EXPECTED);
    }

    public boolean isInterimBooked() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.INTERIM_BOOKED);
    }

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(IngBaseConstants.BalanceTypes.CLOSING_BOOKED);
    }

    public String getCurrency() {
        return balanceAmount.getCurrency();
    }

    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
