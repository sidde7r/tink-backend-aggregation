package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultHisalBalance implements HisalBalance {

    @Override
    public BalanceModule calculate(HISAL hisal) {

        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(hisal.getFirstBalanceValue(), hisal.getCurrency()))
                .setAvailableBalance(ExactCurrencyAmount.of(getBalance(hisal), hisal.getCurrency()))
                .build();
    }

    private BigDecimal getBalance(HISAL hisal) {
        BigDecimal pendingBalance =
                Optional.ofNullable(hisal.getSecondBalanceValue()).orElse(BigDecimal.valueOf(0));
        return hisal.getFirstBalanceValue().add(pendingBalance);
    }
}
