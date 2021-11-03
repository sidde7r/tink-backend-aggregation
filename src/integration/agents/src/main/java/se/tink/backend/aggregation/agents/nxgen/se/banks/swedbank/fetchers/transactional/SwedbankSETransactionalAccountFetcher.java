package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PensionPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.MenuItemKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.SavingAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class SwedbankSETransactionalAccountFetcher
        extends SwedbankDefaultTransactionalAccountFetcher {

    private final SwedbankSEApiClient apiClient;
    private List<String> investmentAccountNumbers;

    public SwedbankSETransactionalAccountFetcher(
            SwedbankSEApiClient apiClient, PersistentStorage persistentStorage) {
        super(apiClient, persistentStorage);
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            EngagementOverviewResponse engagementOverviewResponse =
                    bankProfile.getEngagementOverViewResponse();

            accounts.addAll(getTransactionAccounts(bankProfile, engagementOverviewResponse));

            accounts.addAll(
                    getTransactionDisposalAccounts(bankProfile, engagementOverviewResponse));

            accounts.addAll(getSavingsAccounts(bankProfile, engagementOverviewResponse));

            investmentAccountNumbers = null;
        }

        if (apiClient.getBankProfiles().size() > 1) {
            debugLogAccounts(accounts);
        }

        return accounts;
    }

    private List<TransactionalAccount> getSavingsAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getSavingAccounts().stream()
                .filter(account -> !isInvestmentAccount(account))
                .map(
                        account -> {
                            tryAccessPensionPortfoliosIfPensionType(account);
                            return account.toTransactionalAccount(
                                    bankProfile, getEngagementTransactionsResponse(account));
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<TransactionalAccount> getTransactionDisposalAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getTransactionDisposalAccounts().stream()
                .filter(account -> !isInvestmentAccount(account))
                .map(
                        account ->
                                account.toTransactionalAccount(
                                        bankProfile, getEngagementTransactionsResponse(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<TransactionalAccount> getTransactionAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getTransactionAccounts().stream()
                .filter(account -> !isInvestmentAccount(account))
                .map(
                        account ->
                                account.toTransactionalAccount(
                                        bankProfile, getEngagementTransactionsResponse(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private EngagementTransactionsResponse getEngagementTransactionsResponse(
            AccountEntity account) {
        LinkEntity linkEntity = account.getLinkOrNull();
        if (linkEntity != null) {
            return apiClient.engagementTransactions(linkEntity);
        }
        return null;
    }

    private boolean isInvestmentAccount(AccountEntity account) {
        boolean existsInInvestmentAccounts =
                getInvestmentAccountNumbers().contains(account.getFullyFormattedNumber());
        if (existsInInvestmentAccounts && !account.isInvestmentAccount()) {
            // Mismatched ids should be added to ACCOUNT_TYPE_MAPPER as INVESTMENT
            log.warn("Swedbank investment account product ID mismatch: {}", account.getProductId());
        }
        return existsInInvestmentAccounts;
    }

    private void tryAccessPensionPortfoliosIfPensionType(SavingAccountEntity account) {
        if (SwedbankBaseConstants.SavingAccountTypes.PENSION.equalsIgnoreCase(account.getType())) {
            try {
                PensionPortfoliosResponse pensionPortfolios = apiClient.getPensionPortfolios();
                if (pensionPortfolios.hasPensionHoldings()) {
                    log.info("Holdings found for pension portfolios");
                }
            } catch (Exception e) {
                // Log that we got an exception here so we know about weird behaviour
                log.info("Couldn't fetch pension portfolios: Cause: ", e);
            }
        }
    }

    // fetch all account numbers from investment and pension accounts EXCLUDING savings accounts,
    // this is because we want savings accounts to be fetched by transactional fetcher to get any
    // transactions
    private List<String> getInvestmentAccountNumbers() {
        PortfolioHoldingsResponse portfolioHoldings;
        PensionPortfoliosResponse pensionPortfolios;
        if (investmentAccountNumbers == null) {
            investmentAccountNumbers = new ArrayList<>();
            // sometimes we don't have PortfolioHoldings menuItem, for example if it is
            // youthProfile
            if (apiClient.getBankProfileHandler().isAuthorizedForAction(MenuItemKey.PORTFOLIOS)) {
                portfolioHoldings = apiClient.portfolioHoldings();
                investmentAccountNumbers.addAll(portfolioHoldings.getInvestmentAccountNumbers());
            }
            if (apiClient
                    .getBankProfileHandler()
                    .isAuthorizedForAction(MenuItemKey.PENSION_PORTFOLIOS)) {
                pensionPortfolios = apiClient.getPensionPortfolios();
                investmentAccountNumbers.addAll(pensionPortfolios.getPensionAccountNumbers());
            }
        }

        return investmentAccountNumbers;
    }
}
