package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
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
        final HolderName holderName = new HolderName(temporaryStorage.get(StorageKeys.HOLDER_NAME));

        authSessionStorage.keySet().stream().flatMap(getAccounts(holderName)).close();
        return Collections.emptyList();
    }

    private Function<String, Stream<?>> getAccounts(HolderName holderName) {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isLoanAccount)
                        .peek(
                                acc ->
                                        LOGGER.info(
                                                "Avanza Loan Account: {}",
                                                SerializationUtils.serializeToString(acc)))
                        .map(AccountEntity::getAccountId)
                        .map(accId -> apiClient.fetchAccountDetails(accId, authSession))
                        .peek(
                                details ->
                                        LOGGER.info(
                                                "Avanza Loan Account details: {}",
                                                SerializationUtils.serializeToString(details)));
    }
}
