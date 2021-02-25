package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankBondsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankDistributionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankFundsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankStocksEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PortfolioDetailsResponse extends OpBankResponseEntity {
    private String portfolioId;
    private String name;
    private boolean isPensionPortfolio;
    private String currency;
    private OpBankStocksEntity instrumentGroupStocks;
    private OpBankFundsEntity instrumentGroupFunds;
    private OpBankBondsEntity instrumentGroupBonds;
    private OpBankDistributionEntity distributionLine;

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount() {
        return InvestmentAccount.builder(portfolioId)
                .setCashBalance(ExactCurrencyAmount.zero(currency))
                .setAccountNumber(portfolioId)
                .setBankIdentifier(portfolioId)
                .setPortfolios(Lists.newArrayList(getTinkPortfolio()))
                .setName(portfolioId)
                .build();
    }

    private Portfolio getTinkPortfolio() {

        List<Instrument> instruments = new ArrayList<>();
        if (instrumentGroupFunds != null) {
            instruments.addAll(instrumentGroupFunds.getTinkInstruments());
        }
        if (instrumentGroupStocks != null) {
            instruments.addAll(instrumentGroupStocks.getTinkInstruments());
        }
        if (instrumentGroupBonds != null) {
            instruments.addAll(instrumentGroupBonds.getTinkInstruments());
        }
        Portfolio portfolio = new Portfolio();
        portfolio.setInstruments(instruments);
        portfolio.setType(getPortfolioType());
        portfolio.setTotalValue(distributionLine.getMarketValue().getAmount());
        portfolio.setUniqueIdentifier(portfolioId);
        portfolio.setTotalProfit(distributionLine.getWinLoseEur().getAmount());

        return portfolio;
    }

    @JsonIgnore
    private Portfolio.Type getPortfolioType() {
        return isPensionPortfolio ? Portfolio.Type.PENSION : Portfolio.Type.DEPOT;
    }

    @JsonIgnore
    public boolean hasInvestmentGroups() {
        return instrumentGroupStocks != null
                || instrumentGroupFunds != null
                || instrumentGroupBonds != null;
    }
}
