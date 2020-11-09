package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final IngApiClient ingApiClient;

    public IngInvestmentAccountFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        return this.ingApiClient.getApiRestProducts().getProducts().stream()
                .filter(Product::isActiveInvestmentAccount)
                .map(product -> mapProductToInvestmentAccount(product))
                .collect(Collectors.toList());
    }

    private InvestmentAccount mapProductToInvestmentAccount(Product product) {

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(product.getProductNumber());
        instrument.setPrice(product.getLastNetAssetValue());
        instrument.setQuantity(product.getNumberOfShares());
        instrument.setType(Instrument.Type.FUND);
        instrument.setCurrency(product.getCurrency());
        instrument.setMarketValue(product.getBalance().doubleValue());
        if (product.getNumberOfShares() > 0) {
            instrument.setAverageAcquisitionPrice(
                    product.getInvestment() / product.getNumberOfShares());
        } else {
            instrument.setAverageAcquisitionPrice(BigDecimal.ZERO);
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(product.getProductNumber());
        portfolio.setTotalProfit(product.getPerformance());
        portfolio.setCashValue(0.0);
        portfolio.setTotalValue(product.getBalance().doubleValue());

        if (IngConstants.AccountTypes.INVESTMENT_FUND.equals(product.getType())) {
            portfolio.setType(Portfolio.Type.DEPOT);
        } else if (IngConstants.AccountTypes.PENSION_PLAN.equals(product.getType())) {
            portfolio.setType(Portfolio.Type.PENSION);
        } else {
            portfolio.setType(Portfolio.Type.OTHER);
        }

        portfolio.setInstruments(Collections.singletonList(instrument));

        return InvestmentAccount.builder(product.getProductNumber())
                .setAccountNumber(product.getProductNumber())
                .setCashBalance(ExactCurrencyAmount.zero(product.getCurrency()))
                .setPortfolios(Collections.singletonList(portfolio))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolders().get(0).getAnyName()))
                .setBankIdentifier(product.getUuid())
                .setName(product.getName())
                .build();
    }
}
