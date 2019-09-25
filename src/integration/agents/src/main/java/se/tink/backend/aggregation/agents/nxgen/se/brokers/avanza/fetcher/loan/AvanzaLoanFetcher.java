package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.SessionAccountPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AvanzaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaLoanFetcher.class);

    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaLoanFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final List<SessionAccountPair> sessionAccountPairs =
                authSessionStorage.keySet().stream()
                        .flatMap(getSessionAccountPairs())
                        .collect(Collectors.toList());
        sessionAccountPairs.forEach(this::getAccounts);
        return Collections.emptyList();
    }

    private Function<String, Stream<? extends SessionAccountPair>> getSessionAccountPairs() {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isInvestmentAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> new SessionAccountPair(authSession, accountId));
    }

    private Function<String, Stream<? extends LoanAccount>> getAccounts(
            SessionAccountPair sessionAccount) {

        final String authSession = sessionAccount.getAuthSession();
        List<AccountEntity> accounts =
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isLoanAccount)
                        .collect(Collectors.toList());
        LOGGER.info("Avanza Loan Accounts: {}", SerializationUtils.serializeToString(accounts));
        List<AccountDetailsResponse> accountDetails =
                accounts.stream()
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> apiClient.fetchAccountDetails(accountId, authSession))
                        .collect(Collectors.toList());
        LOGGER.info(
                "Avanza Loan Account details: {}",
                SerializationUtils.serializeToString(accountDetails));
        throw new NotImplementedException(
                "Implementation not complete, logging response entities only for now until we can implement!");
    }
}
