package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngHisalBalance implements HisalBalance {

    @Override
    public BalanceModule calculate(HISAL hisal) {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(hisal.getBookedBalance(), hisal.getCurrency()))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(hisal.getPendingBalance(), hisal.getCurrency()))
                .build();
    }
}
