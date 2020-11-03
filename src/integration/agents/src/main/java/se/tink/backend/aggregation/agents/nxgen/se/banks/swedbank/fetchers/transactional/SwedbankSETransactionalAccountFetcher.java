package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PensionPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.SavingAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankSETransactionalAccountFetcher
        extends SwedbankDefaultTransactionalAccountFetcher {
    private static final Logger log =
            LoggerFactory.getLogger(SwedbankSETransactionalAccountFetcher.class);

    private SwedbankSEApiClient apiClient;
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

            accounts.addAll(
                    engagementOverviewResponse.getTransactionAccounts().stream()
                            .map(account -> account.toTransactionalAccount(bankProfile))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
            accounts.addAll(
                    engagementOverviewResponse.getTransactionDisposalAccounts().stream()
                            .map(account -> account.toTransactionalAccount(bankProfile))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
            accounts.addAll(
                    engagementOverviewResponse.getSavingAccounts().stream()
                            // have not found any other way to filter out investment accounts from
                            // savings accounts
                            .filter(
                                    account ->
                                            !getInvestmentAccountNumbers()
                                                    .contains(account.getFullyFormattedNumber()))
                            .map(
                                    account -> {
                                        tryAccessPensionPortfoliosIfPensionType(account);
                                        return account.toTransactionalAccount(bankProfile);
                                    })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
            investmentAccountNumbers = null;
        }

        if (apiClient.getBankProfiles().size() > 1) {
            debugLogAccounts(accounts);
        }

        return accounts;
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

    // fetch all account number from investment accounts BUT savings accounts, this is because we
    // want savings accounts
    // to be fetched by transactional fetcher to get any transactions
    private List<String> getInvestmentAccountNumbers() {

        if (investmentAccountNumbers == null) {
            PortfolioHoldingsResponse portfolioHoldings = apiClient.portfolioHoldings();
            investmentAccountNumbers = portfolioHoldings.investmentAccountNumbers();
        }

        return investmentAccountNumbers;
    }
}
