package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.rpc.InvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class BankinterInvestmentFetcher
        implements AccountFetcher<InvestmentAccount>,
                TransactionKeyPaginator<InvestmentAccount, String> {
    private final BankinterApiClient apiClient;

    public BankinterInvestmentFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final GlobalPositionResponse globalPosition = apiClient.fetchGlobalPosition();
        ArrayList<InvestmentAccount> accounts = new ArrayList<>();
        for (String url : globalPosition.getInvestmentLinks()) {
            final InvestmentResponse investmentResponse = apiClient.fetchInvestmentAccount(url);
            accounts.add(toInvestmentAccount(investmentResponse));
        }
        return accounts;
    }

    public static InvestmentAccount toInvestmentAccount(InvestmentResponse response) {
        final InstrumentModule instrument =
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(
                                InstrumentIdModule.of(
                                        response.getIsin(),
                                        null,
                                        response.getName(),
                                        response.getFundAccount()))
                        .withMarketPrice(
                                response.getNumberOfShares()
                                        .multiply(response.getSharePrice())
                                        .doubleValue())
                        .withMarketValue(response.getTotalBalance().doubleValue())
                        .withAverageAcquisitionPrice(
                                response.getContributions()
                                        .divide(
                                                response.getNumberOfShares(),
                                                2,
                                                BigDecimal.ROUND_HALF_UP)
                                        .doubleValue())
                        .withCurrency(response.getCurrency())
                        .withQuantity(response.getNumberOfShares().doubleValue())
                        .withProfit(response.getProfit().doubleValue())
                        .build();

        final PortfolioModule portfolio =
                PortfolioModule.builder()
                        .withType(PortfolioType.OTHER)
                        .withUniqueIdentifier(response.getFundAccount())
                        .withCashValue(0)
                        .withTotalProfit(response.getProfit().doubleValue())
                        .withTotalValue(response.getTotalBalance().doubleValue())
                        .withInstruments(instrument)
                        .build();

        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolio)
                .withZeroCashBalance(response.getCurrency())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(response.getFundAccount())
                                .withAccountNumber(response.getFundAccount())
                                .withAccountName(response.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.BBAN,
                                                response.getFundAccount()))
                                .build())
                .putInTemporaryStorage(StorageKeys.RESPONSE_BODY, response.getBody())
                .build();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            InvestmentAccount account, String key) {
        final InvestmentResponse response =
                new InvestmentResponse(
                        account.getFromTemporaryStorage(StorageKeys.RESPONSE_BODY, String.class)
                                .get());

        return new TransactionKeyPaginatorResponseImpl<String>(response.toTinkTransactions(), null);
    }
}
