package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date lastChangeDateTime;

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(
                new Double(balanceAmount.getAmount()), balanceAmount.getCurrency());
    }
}
