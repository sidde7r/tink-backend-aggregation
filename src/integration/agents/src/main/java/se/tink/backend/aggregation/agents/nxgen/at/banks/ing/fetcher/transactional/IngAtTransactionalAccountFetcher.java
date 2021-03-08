package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtTransactionalAccountParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
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
        final Collection<TransactionalAccount> res = new ArrayList<>();
        for (AccountReferenceEntity accountReference : transactionalAccountReferences) {
            final HttpResponse response =
                    apiClient.getAccountDetails(new URL(accountReference.getUrl()));
            final IngAtTransactionalAccountParser parser =
                    new IngAtTransactionalAccountParser(response.getBody(String.class));
            final IbanIdentifier ibanId =
                    parser.getBic().isPresent()
                            ? new IbanIdentifier(parser.getBic().get(), parser.getIban())
                            : new IbanIdentifier(parser.getIban());
            TransactionalAccount.Builder builder =
                    TransactionalAccount.builder(
                            AccountTypes.valueOf(accountReference.getType()),
                            accountReference.getId(),
                            parser.getAmount());
            builder.setAccountNumber(accountReference.getId()).addIdentifier(ibanId);

            Optional.ofNullable(accountReference.getAccountName()).ifPresent(builder::setName);

            Optional.ofNullable(webLoginResponse.getAccountHolder())
                    .ifPresent(holder -> builder.setHolderName(new HolderName(holder)));

            builder.putInTemporaryStorage(
                    IngAtConstants.Storage.ACCOUNT_INDEX.name(),
                    accountReference.getAccountIndex());

            res.add(builder.build());
        }

        return res;
    }
}
