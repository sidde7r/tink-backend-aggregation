package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

/**
 * Natwest Group Credit Cards accounts interpret CreditLineIndicator values (Debit/Credit)
 * differently than other banks, so we need to flip the sign of the balance amount
 * https://openapi.ulsterbank.co.uk/bankofapis/v1.0/dynamic-content/content/assets/release-notes/2020-05-12-Release-Notes.pdf
 */
@Slf4j
public class NatwestGroupBalancePreCalculator implements BalancePreCalculator {

    private final BalancePreCalculator defaultCalculator;

    public NatwestGroupBalancePreCalculator() {
        this.defaultCalculator = new DefaultBalancePreCalculator();
    }

    @Override
    public ExactCurrencyAmount calculateBalanceAmountConsideringCreditLines(
            UkObBalanceType balanceType,
            ExactCurrencyAmount balanceAmount,
            List<CreditLineEntity> creditLines) {
        return defaultCalculator.calculateBalanceAmountConsideringCreditLines(
                balanceType, balanceAmount.negate(), creditLines);
    }
}
