package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional;

import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.from;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtTransactionalAccountParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class IngAtTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private IngAtApiClient apiClient;
    private IngAtSessionStorage sessionStorage;

    public IngAtTransactionalAccountFetcher(
            final IngAtApiClient apiClient, final IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private static boolean isTransactionalAccountType(AccountReferenceEntity r) {
        return IngAtConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                r.getType(), TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final WebLoginResponse webLoginResponse =
                sessionStorage
                        .getWebLoginResponse()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find login response when fetching accounts"));
        final List<AccountReferenceEntity> transactionalAccountReferences =
                webLoginResponse.getAccountReferenceEntities().stream()
                        .filter(IngAtTransactionalAccountFetcher::isTransactionalAccountType)
                        .collect(Collectors.toList());
        final Collection<TransactionalAccount> transactionalAccounts = new ArrayList<>();
        for (AccountReferenceEntity accountReference : transactionalAccountReferences) {
            final HttpResponse response =
                    apiClient.getAccountDetails(new URL(accountReference.getUrl()));
            final IngAtTransactionalAccountParser parser =
                    new IngAtTransactionalAccountParser(response.getBody(String.class));

            AccountTypes accountType = AccountTypes.valueOf(accountReference.getType());
            TransactionalAccountType transactionalAccountType = from(accountType).orElse(null);

            TransactionalAccount transactionalAccount =
                    TransactionalAccount.nxBuilder()
                            .withType(transactionalAccountType)
                            .withPaymentAccountFlag()
                            .withBalance(BalanceModule.of(parser.getAmount()))
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(accountReference.getId())
                                            .withAccountNumber(accountReference.getId())
                                            .withAccountName(accountReference.getAccountName())
                                            .addIdentifier(getIban(parser))
                                            .build())
                            .addHolderName(webLoginResponse.getAccountHolder())
                            .putInTemporaryStorage(
                                    IngAtConstants.Storage.ACCOUNT_INDEX.name(),
                                    accountReference.getAccountIndex())
                            .build()
                            .orElse(null);

            transactionalAccounts.add(transactionalAccount);
        }

        return transactionalAccounts;
    }

    private IbanIdentifier getIban(IngAtTransactionalAccountParser parser) {
        return parser.getBic().isPresent()
                ? new IbanIdentifier(parser.getBic().get(), parser.getIban())
                : new IbanIdentifier(parser.getIban());
    }
}
