package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Assets;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Investment;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.util.CurrencyMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final SantanderApiClient apiClient;
    private final CurrencyMapper currencyMapper;

    public SantanderInvestmentAccountFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Map<String, Object> assets =
                (Map<String, Object>) apiClient.fetchAssets().getBusinessData().get(0);

        List<Map<String, String>> investmentAccounts =
                (List<Map<String, String>>) assets.get(Assets.INVESTMENT_ACCOUNTS);
        List<Map<String, String>> retirementInvestmentAccounts =
                (List<Map<String, String>>) assets.get(Assets.RETIREMENT_INVESTMENTS);

        return Stream.concat(
                        investmentAccounts.stream()
                                .map(account -> toTinkAccount(account, PortfolioType.DEPOT)),
                        retirementInvestmentAccounts.stream()
                                .map(account -> toTinkAccount(account, PortfolioType.PENSION)))
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), Collections::unmodifiableList));
    }

    private InvestmentAccount toTinkAccount(
            Map<String, String> account, PortfolioType portfolioType) {

        double totalProfit =
                calculateTotalProfit(
                        account.get(Investment.AVAILABLE_BALANCE), account.get(Investment.BALANCE));

        String accountCurrencyCode =
                currencyMapper
                        .get(Integer.parseInt(account.get(Investment.CURRENCY_NUMERIC_CODE)))
                        .getCurrencyCode();

        return InvestmentAccount.nxBuilder()
                .withPortfolios(
                        PortfolioModule.builder()
                                .withType(portfolioType)
                                .withUniqueIdentifier(account.get(Investment.ACCOUNT_NUMBER))
                                .withCashValue(BigDecimal.ZERO.doubleValue())
                                .withTotalProfit(totalProfit)
                                .withTotalValue(
                                        new BigDecimal(account.get(Investment.AVAILABLE_BALANCE))
                                                .doubleValue())
                                .withoutInstruments()
                                .build())
                .withCashBalance(
                        ExactCurrencyAmount.of(
                                account.get(Investment.AVAILABLE_BALANCE), accountCurrencyCode))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(account.get(Investment.FULL_ACCOUNT_NUMBER))
                                .withAccountNumber(account.get(Investment.ACCOUNT_NUMBER))
                                .withAccountName(account.get(Investment.PRODUCT_NAME).trim())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.IBAN, account.get(Investment.ACCOUNT_NUMBER)))
                                .build())
                .build();
    }

    private double calculateTotalProfit(String availableBalance, String balance) {
        return new BigDecimal(availableBalance).subtract(new BigDecimal(balance)).doubleValue();
    }
}
