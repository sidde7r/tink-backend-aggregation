package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import com.google.common.collect.Lists;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class InvestmentAccountPortfolioResponse extends PortfolioEntity {
    public InvestmentAccount toTinkInvestmentAccount(HolderName holderName, Portfolio portfolio) {
        return InvestmentAccount.builder(getAccountId())
                .setAccountNumber(getAccountId())
                .setName(getAccountType())
                .setHolderName(holderName)
                .setCashBalance(Amount.inSEK(getTotalBuyingPower()))
                .setBankIdentifier(getAccountId())
                .setPortfolios(Lists.newArrayList(portfolio))
                .build();
    }
}
