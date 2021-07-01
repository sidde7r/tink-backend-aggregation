package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@JsonObject
public class SwedbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TransactionPaginationHelper transactionPaginationHelper;
    private final AgentComponentProvider componentProvider;
    private FetchAccountResponse fetchAccountResponse;

    public SwedbankTransactionalAccountFetcher(
            SwedbankApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TransactionPaginationHelper transactionPaginationHelper,
            AgentComponentProvider componentProvider) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.transactionPaginationHelper = transactionPaginationHelper;
        this.componentProvider = componentProvider;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        Collection<TransactionalAccount> tinkAccounts =
                getAccounts().getAccountList().stream()
                        .map(toTinkAccountWithBalance())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        // Request transactions over 90 days zip file in advance
        tinkAccounts.forEach(this::postAccountStatement);

        return tinkAccounts;
    }

    private Function<AccountEntity, Optional<TransactionalAccount>> toTinkAccountWithBalance() {
        return account -> {
            if (account.getBalances() != null && !account.getBalances().isEmpty()) {
                return account.toTinkAccount(account.getBalances());
            } else {
                return account.toTinkAccount(
                        apiClient.getAccountBalance(account.getResourceId()).getBalances());
            }
        };
    }

    private void handleConsentFlow() {
        try {
            if (apiClient.isConsentValid()) {
                return;
            }
        } catch (HttpResponseException e) {
            handleFetchAccountError(e);
        }

        // All auth consent (is it ok to store both consents in one place?)
        useConsent(apiClient.getConsentAllAccounts().getConsentId());

        // Detailed consent
        getDetailedConsent(getAccounts())
                .ifPresent(
                        consentResponse -> {
                            String status = consentResponse.getConsentStatus();
                            String consentId = consentResponse.getConsentId();

                            if (ConsentStatus.VALID.equalsIgnoreCase(status)) {
                                useConsent(consentId);
                            } else {
                                // SCA Authentication for case without granted scopes
                                try {
                                    handleConsentAuthentication(consentResponse);
                                } catch (HttpResponseException e) {
                                    handleFetchAccountError(e);
                                }
                            }
                        });
    }

    private void handleConsentAuthentication(ConsentResponse consentResponse) {
        String url = consentResponse.getLinks().getHrefEntity().getHref();
        AuthenticationResponse authResponse = apiClient.authorizeConsent(url);

        Uninterruptibles.sleepUninterruptibly(
                TimeValues.SCA_STATUS_POLL_DELAY, TimeUnit.MILLISECONDS);

        for (int i = 0; i < TimeValues.SCA_STATUS_POLL_MAX_ATTEMPTS; i++) {
            String status = apiClient.getScaStatus(authResponse.getCollectAuthUri());

            switch (status.toLowerCase()) {
                case AuthStatus.RECEIVED:
                case AuthStatus.STARTED:
                    logger.warn("Waiting for authentication");
                    break;
                case AuthStatus.FINALIZED:
                    useConsent(consentResponse.getConsentId());
                    return;
                case AuthStatus.FAILED:
                    throw AuthorizationError.UNAUTHORIZED.exception();
                default:
                    logger.warn("Unknown status {}", status);
                    throw AuthorizationError.UNAUTHORIZED.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    TimeValues.SCA_STATUS_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        logger.warn("Timeout");
    }

    private FetchAccountResponse getAccounts() {
        try {
            if (fetchAccountResponse == null) {
                handleConsentFlow();
                fetchAccountResponse = apiClient.fetchAccounts();
            }
            return fetchAccountResponse;
        } catch (HttpResponseException e) {
            handleFetchAccountError(e);
            throw e;
        }
    }

    public boolean isCrossLogin() {
        return !getAccounts().getAccountList().isEmpty()
                && SwedbankConstants.BANK_IDS
                        .get(0)
                        .equals(fetchAccountResponse.getAccountList().get(0).getBankId().trim());
    }

    private Optional<ConsentResponse> getDetailedConsent(
            FetchAccountResponse fetchAccountResponse) {

        return fetchAccountResponse.getAccountList().isEmpty()
                ? Optional.empty()
                : Optional.of(
                        apiClient.getConsentAccountDetails(
                                mapAccountResponseToIbanList(fetchAccountResponse)));
    }

    private List<String> mapAccountResponseToIbanList(FetchAccountResponse accounts) {
        return accounts.getAccountList().stream()
                .map(AccountEntity::getIban)
                .collect(Collectors.toList());
    }

    private void handleFetchAccountError(HttpResponseException e) {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);

        if (errorResponse.isConsentInvalid()
                || errorResponse.isResourceUnknown()
                || errorResponse.isConsentExpired()) {
            removeConsent();
        }

        if (errorResponse.isKycError() || errorResponse.isMissingBankAgreement()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
    }

    private void useConsent(String consentId) {
        persistentStorage.put(SwedbankConstants.StorageKeys.CONSENT, consentId);
    }

    private void removeConsent() {
        // Use the consent ID for communication with Swedbank
        log.info(
                "Remvoving invalid consent with ID = {}",
                persistentStorage.get(SwedbankConstants.StorageKeys.CONSENT));
        persistentStorage.remove(SwedbankConstants.StorageKeys.CONSENT);
    }

    private void postAccountStatement(Account account) {
        Optional<Date> certainDate = transactionPaginationHelper.getTransactionDateLimit(account);
        final LocalDate fromDate =
                componentProvider
                        .getLocalDateTimeSource()
                        .now()
                        .minusMonths(TimeValues.MONTHS_TO_FETCH_MAX)
                        .toLocalDate();
        final LocalDate toDate =
                componentProvider
                        .getLocalDateTimeSource()
                        .now()
                        .minusDays(TimeValues.ONLINE_STATEMENT_MAX_DAYS)
                        .toLocalDate();

        // No need to fetch transactions if the account was refreshed within the last 90 days
        if (certainDate.isPresent()
                && certainDate
                        .get()
                        .after(
                                Date.from(
                                        toDate.atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()))) {
            return;
        }

        Optional<StatementResponse> response =
                apiClient.postOrGetOfflineStatement(account.getApiIdentifier(), fromDate, toDate);
        if (!response.isPresent()) {
            return;
        }
        sessionStorage.put(account.getApiIdentifier(), response.get());
    }
}
