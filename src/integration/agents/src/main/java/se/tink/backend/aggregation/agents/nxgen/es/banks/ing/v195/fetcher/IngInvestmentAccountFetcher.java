package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class IngInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final IngApiClient ingApiClient;

    public IngInvestmentAccountFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        return this.ingApiClient.getApiRestProducts()
                .getProducts()
                .stream()
                .filter(IngInvestmentAccountFetcher::filterInvetmentAccounts)
                .map(IngInvestmentAccountFetcher::mapProductToInvestmentAccount)
                .collect(Collectors.toList());
    }

    private static boolean filterInvetmentAccounts(Product product) {
        boolean isInvestmentAccount = IngConstants.AccountCategories.INVESTMENT.contains(product.getType());

        boolean isActive = IngConstants.AccountStatus.OPERATIVE.equals(product.getStatus().getCod());

        return isInvestmentAccount && isActive;
    }

    private static InvestmentAccount mapProductToInvestmentAccount(Product product) {

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(product.getProductNumber());
        instrument.setPrice(product.getLastNetAssetValue());
        instrument.setQuantity(product.getNumberOfShares());
        instrument.setType(Instrument.Type.FUND);
        instrument.setCurrency(product.getCurrency());
        instrument.setMarketValue(product.getBalance());
        if (product.getNumberOfShares() > 0) {
            instrument.setAverageAcquisitionPrice(product.getInvestment() / product.getNumberOfShares());
        } else {
            instrument.setAverageAcquisitionPrice(0D);
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(product.getProductNumber());
        portfolio.setTotalProfit(product.getPerformance());
        portfolio.setCashValue(0.0);
        portfolio.setTotalValue(product.getBalance());

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
                .setCashBalance(new Amount(product.getCurrency(), 0.0))
                .setPortfolios(Collections.singletonList(portfolio))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolders().get(0).getAnyName()))
                .setBankIdentifier(product.getUuid())
                .setName(product.getName())
                .build();
    }
}
