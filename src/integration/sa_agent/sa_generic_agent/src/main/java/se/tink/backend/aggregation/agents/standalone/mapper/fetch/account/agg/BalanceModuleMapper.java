package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

import java.math.BigDecimal;

public class BalanceModuleMapper implements Mapper<BalanceModule, se.tink.sa.services.fetch.account.BalanceModule> {

    @Override
    public BalanceModule map(se.tink.sa.services.fetch.account.BalanceModule source, MappingContext mappingContext) {
        ExactCurrencyAmount exactBalance = source.getExactBalance();
        se.tink.libraries.amount.ExactCurrencyAmount amount = se.tink.libraries.amount.ExactCurrencyAmount.of(BigDecimal.valueOf(exactBalance.getValue().getUnscaledValue(), exactBalance.getValue().getScale()), exactBalance.getCurrencyCode());
        return BalanceModule.of(amount);
    }
}
