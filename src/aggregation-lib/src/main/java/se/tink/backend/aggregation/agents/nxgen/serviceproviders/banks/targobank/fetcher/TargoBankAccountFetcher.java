package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.account.AccountIdentifier;

public class TargoBankAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankAccountFetcher.class);
    private final TargoBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankAccountFetcher(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankAccountFetcher create(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        return new TargoBankAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountSummaryResponse details = this.sessionStorage
                .get(TargoBankConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElseGet(() -> this.apiClient.requestAccounts());
        return details
                .getAccountDetailsList()
                .stream()
                .filter(a -> {
                    AccountTypes tinkType = a.getTinkTypeByTypeNumber().getTinkType();
                    return AccountTypes.CHECKING.equals(tinkType) || AccountTypes.SAVINGS.equals(tinkType);
                })
                .map(a -> {
                    TransactionalAccount.Builder<TransactionalAccount, ?> accountBuilder = (TransactionalAccount.Builder) a
                            .getAccountBuilder();
                    return accountBuilder
                            .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, a.getIban()))
                            .setUniqueIdentifier(a.getIban().toLowerCase())
                            .setName(a.getAccountName())
                            .setAccountNumber(a.getAccountNumber().toLowerCase())
                            .addToTemporaryStorage(TargoBankConstants.Tags.WEB_ID, a.getWebId())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
