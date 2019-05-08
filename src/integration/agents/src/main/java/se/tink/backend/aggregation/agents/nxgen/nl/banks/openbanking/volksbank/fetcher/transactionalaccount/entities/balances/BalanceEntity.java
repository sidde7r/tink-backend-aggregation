package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date lastChangeDateTime;

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public Optional<Amount> toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(
                        b ->
                                new Amount(
                                        balanceAmount.getCurrency(),
                                        new Double(balanceAmount.getAmount())));
    }
}
