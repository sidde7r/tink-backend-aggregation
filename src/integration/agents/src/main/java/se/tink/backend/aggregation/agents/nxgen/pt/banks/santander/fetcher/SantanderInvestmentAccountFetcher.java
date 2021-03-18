package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Assets;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Deposit;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Investment;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
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
        List<Map<String, String>> deposits =
                (List<Map<String, String>>) assets.get(Assets.DEPOSITS);

        List<InvestmentAccount> allInvestments = new ArrayList<>();

        investmentAccounts.forEach(
                account -> allInvestments.add(mapInvestment(account, PortfolioType.DEPOT)));

        retirementInvestmentAccounts.forEach(
                account -> allInvestments.add(mapInvestment(account, PortfolioType.PENSION)));

        deposits.forEach(account -> allInvestments.add(mapDeposit(account)));

        return Collections.unmodifiableCollection(allInvestments);
    }

    private InvestmentAccount mapInvestment(
            Map<String, String> account, PortfolioType portfolioType) {

        PortfolioModule portfolio = buildInvestmentPortfolio(account, portfolioType);
        return buildAccount(account, portfolio);
    }

    private InvestmentAccount mapDeposit(Map<String, String> account) {
        PortfolioModule portfolio = buildDepositPortfolio(account);
        return buildAccount(account, portfolio);
    }

    private InvestmentAccount buildAccount(Map<String, String> account, PortfolioModule portfolio) {

        String accountCurrencyCode =
                currencyMapper
                        .get(Integer.parseInt(account.get(Investment.CURRENCY_NUMERIC_CODE)))
                        .getCurrencyCode();

        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolio)
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
                                                AccountIdentifierType.IBAN,
                                                account.get(Investment.ACCOUNT_NUMBER)))
                                .build())
                .build();
    }

    private PortfolioModule buildInvestmentPortfolio(
            Map<String, String> account, PortfolioType portfolioType) {

        double totalProfit =
                calculateTotalInvestmentProfit(
                        account.get(Investment.AVAILABLE_BALANCE), account.get(Investment.BALANCE));

        return PortfolioModule.builder()
                .withType(portfolioType)
                .withUniqueIdentifier(account.get(Investment.ACCOUNT_NUMBER))
                .withCashValue(BigDecimal.ZERO.doubleValue())
                .withTotalProfit(totalProfit)
                .withTotalValue(
                        new BigDecimal(account.get(Investment.AVAILABLE_BALANCE)).doubleValue())
                .withoutInstruments()
                .build();
    }

    private PortfolioModule buildDepositPortfolio(Map<String, String> depositAccount) {
        Map<String, String> depositDetails =
                (Map<String, String>)
                        apiClient
                                .fetchDepositDetails(
                                        depositAccount.get(Investment.ACCOUNT_NUMBER),
                                        depositAccount.get(Investment.BRANCH_CODE))
                                .getBusinessData()
                                .get(0);

        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(depositAccount.get(Investment.ACCOUNT_NUMBER))
                .withCashValue(Double.parseDouble(depositAccount.get(Deposit.BALANCE)))
                .withTotalProfit(Double.parseDouble(depositDetails.get(Deposit.GROSS_INTEREST)))
                .withTotalValue(new BigDecimal(depositAccount.get(Deposit.BALANCE)).doubleValue())
                .withoutInstruments()
                .build();
    }

    private double calculateTotalInvestmentProfit(String availableBalance, String balance) {
        return new BigDecimal(availableBalance).subtract(new BigDecimal(balance)).doubleValue();
    }
}
