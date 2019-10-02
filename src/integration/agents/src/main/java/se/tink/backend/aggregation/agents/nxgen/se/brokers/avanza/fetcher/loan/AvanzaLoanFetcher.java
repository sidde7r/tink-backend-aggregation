package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
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

        for (String authSession : authSessionStorage.keySet()) {
            getAccounts(authSession);
        }
        return Collections.emptyList();
    }

    private void getAccounts(String authSession) {
        for (AccountEntity account : apiClient.fetchAccounts(authSession).getAccounts()) {
            if (account.isLoanAccount()) {
                LOGGER.info(
                        "Avanza Loan Account: {}", SerializationUtils.serializeToString(account));
                String accId = account.getAccountId();
                Optional<AccountDetailsResponse> details =
                        Optional.ofNullable(apiClient.fetchAccountDetails(accId, authSession));
                if (details.isPresent()) {
                    LOGGER.info(
                            "Avanza Loan Account details: {}",
                            SerializationUtils.serializeToString(details.get()));
                } else {
                    LOGGER.info("No loan details!");
                }
            }
        }
    }
}
