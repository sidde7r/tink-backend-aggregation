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
                .orElse(this.apiClient.requestAccounts());
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
                            .setName(a.getIntX()).setAccountNumber(a.getAccountNumber().toLowerCase())
                            .addToTemporaryStorage(TargoBankConstants.Tags.WEB_ID, a.getWebId()).build();
                })
                .collect(Collectors.toList());
    }

    private AccountSummaryResponse requestAccounts() {
        String body = buildAccountSummaryRequest();
        AccountSummaryResponse details = apiClient.getAccounts(body);
        this.sessionStorage.put(TargoBankConstants.Tags.ACCOUNT_LIST, details);
        return details;
    }

    private String buildAccountSummaryRequest() {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.CATEGORIZE,
                            TargoBankConstants.RequestBodyValues.CATEGORIZE_VALUE)
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }
}
