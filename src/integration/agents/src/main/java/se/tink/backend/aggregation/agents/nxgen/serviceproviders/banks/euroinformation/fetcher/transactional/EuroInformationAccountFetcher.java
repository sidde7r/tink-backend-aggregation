package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;

public class EuroInformationAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EuroInformationAccountFetcher.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationAccountFetcher(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationAccountFetcher create(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountSummaryResponse details =
                this.sessionStorage
                        .get(
                                EuroInformationConstants.Tags.ACCOUNT_LIST,
                                AccountSummaryResponse.class)
                        .orElseGet(() -> this.apiClient.requestAccounts());
        return details.getAccountDetailsList().stream()
                .filter(
                        a -> {
                            AccountTypes tinkType = a.getTinkTypeByTypeNumber().getTinkType();
                            return (AccountTypes.CHECKING == tinkType)
                                    || (AccountTypes.SAVINGS == tinkType);
                        })
                .map(
                        a -> {
                            TransactionalAccount.Builder<TransactionalAccount, ?> accountBuilder =
                                    (TransactionalAccount.Builder) a.getAccountBuilder();
                            return accountBuilder
                                    .addIdentifier(
                                            AccountIdentifier.create(
                                                    AccountIdentifier.Type.IBAN, a.getIban()))
                                    .setName(a.getAccountName())
                                    .setAccountNumber(a.getAccountNumber().toLowerCase())
                                    .putInTemporaryStorage(
                                            EuroInformationConstants.Tags.WEB_ID, a.getWebId())
                                    .build();
                        })
                .collect(Collectors.toList());
    }
}
