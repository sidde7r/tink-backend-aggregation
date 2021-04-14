package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity implements BalanceMappable {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date lastChangeDateTime;

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    @Override
    public boolean isCreditLimitIncluded() {
        return false;
    }

    @Override
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                new BigDecimal(balanceAmount.getAmount()), balanceAmount.getCurrency());
    }

    @Override
    public Optional<BalanceType> getBalanceType() {
        return BalanceType.findByStringType(balanceType);
    }
}
