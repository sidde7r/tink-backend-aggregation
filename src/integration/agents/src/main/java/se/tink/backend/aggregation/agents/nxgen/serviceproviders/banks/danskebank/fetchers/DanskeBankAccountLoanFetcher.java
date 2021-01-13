package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DanskeBankAccountLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;
    private final AccountEntityMapper accountEntityMapper;
    private final boolean shouldFetchMortgages;
    private final String marketCode;

    public DanskeBankAccountLoanFetcher(
            DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration,
            AccountEntityMapper accountEntityMapper,
            boolean shouldFetchMortgages) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.languageCode = configuration.getLanguageCode();
        this.accountEntityMapper = accountEntityMapper;
        this.shouldFetchMortgages = shouldFetchMortgages;
        this.marketCode = configuration.getMarketCode();
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        ListAccountsResponse listAccounts =
                apiClient.listAccounts(ListAccountsRequest.createFromLanguageCode(languageCode));

        // log any loan account of unknown product type
        logUnknownLoanAccountTypes(listAccounts);

        List<LoanAccount> loans =
                listAccounts.getAccounts().stream()
                        .filter(AccountEntity::isLoanAccount)
                        .map(this::toLoanAccount)
                        .distinct()
                        .collect(Collectors.toList());

        if (shouldFetchMortgages) {
            loans.addAll(fetchMortgages());
        }

        return loans;
    }

    private LoanAccount toLoanAccount(AccountEntity account) {
        AccountDetailsResponse accountDetailsResponse =
                fetchLoanAccountDetails(account.getAccountNoInt());
        return accountEntityMapper.toLoanAccount(configuration, account, accountDetailsResponse);
    }

    private void logUnknownLoanAccountTypes(ListAccountsResponse listAccounts) {
        listAccounts.getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .filter(
                        DanskeBankPredicates.knownLoanAccountProducts(
                                        configuration.getLoanAccountTypes())
                                .negate())
                .forEach(
                        a ->
                                logger.info(
                                        "Unknown loan account: apiIdentifier = {}, accountProduct = {}",
                                        a.getAccountNoInt(),
                                        a.getAccountProduct()));
    }

    AccountDetailsResponse fetchLoanAccountDetails(String accountNumberInternal) {
        // Use EN language, because information returned in other languages is gibberish.
        try {
            return apiClient.fetchAccountDetails(
                    new AccountDetailsRequest(accountNumberInternal, "EN"));
        } catch (HttpResponseException e) {
            // Sometimes we receive 500 response that has a body of AccountDetailsResponse including
            // interestRate, but missing other fields like accountOwners and accountType - that's
            // why I think it is 500.
            // Try to get the body, so we could set interest rate
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    && e.getResponse().hasBody()) {
                try {
                    return e.getResponse().getBody(AccountDetailsResponse.class);
                } catch (RuntimeException re) {
                    logger.info("Failed to map exception body into AccountDetailsResponse. ", e);
                }
            }
            logger.info("Failed to fetch loan account details. ", e);
        }
        return new AccountDetailsResponse();
    }

    private Collection<LoanAccount> fetchMortgages() {
        try {
            // this is only mortgages (real estate)
            ListLoansResponse loansResponse =
                    apiClient.listLoans(ListLoansRequest.createFromLanguageCode(languageCode));

            return Optional.ofNullable(loansResponse.getLoans()).orElseGet(Collections::emptyList)
                    .stream()
                    .map(this::mortgageToLoanAccount)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            logger.warn(
                    DanskeBankConstants.LogTags.LOAN_ACCOUNT
                            + " - Failed to fetch loans "
                            + e.getMessage(),
                    e);

            return Collections.emptyList();
        }
    }

    private LoanAccount mortgageToLoanAccount(LoanEntity loan) {
        LoanDetailsResponse loanDetailsResponse =
                apiClient.loanDetails(
                        new LoanDetailsRequest(
                                languageCode, loan.getRealEstateNumber(), loan.getLoanNumber()));

        return loan.toTinkLoan(loanDetailsResponse, marketCode);
    }
}
