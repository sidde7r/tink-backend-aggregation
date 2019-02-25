package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import com.google.common.collect.Lists;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class IngInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final IngApiClient ingApiClient;
    private final SessionStorage sessionStorage;

    public IngInvestmentAccountFetcher(IngApiClient ingApiClient, SessionStorage sessionStorage) {
        this.ingApiClient = ingApiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<Product> products = this.sessionStorage
                .get(IngConstants.Tags.PRODUCT_LIST, ProductsResponse.class)
                .orElseGet(this.ingApiClient::getApiRestProducts)
                .getProducts();

        return products
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
        instrument.setAverageAcquisitionPrice(product.getInvestment() / product.getNumberOfShares());

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(product.getProductNumber());
        portfolio.setTotalProfit(product.getPerformance());
        portfolio.setCashValue(0.0);
        portfolio.setTotalValue(product.getBalance());
        portfolio.setType(Portfolio.Type.OTHER);

        portfolio.setInstruments(Lists.newArrayList(instrument));

        return InvestmentAccount.builder(product.getProductNumber())
                .setAccountNumber(product.getProductNumber())
                .setCashBalance(new Amount(product.getCurrency(), 0.0))
                .setPortfolios(Lists.newArrayList(portfolio))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolders().get(0).getAnyName()))
                .setBankIdentifier(product.getUuid())
                .setName(product.getName())
                .build();
    }
}
