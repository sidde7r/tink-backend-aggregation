package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalancePreCalculator {

    ExactCurrencyAmount calculateBalanceAmountConsideringCreditLines(
            UkObBalanceType balanceType,
            ExactCurrencyAmount balanceAmount,
            List<CreditLineEntity> creditLines);
}
