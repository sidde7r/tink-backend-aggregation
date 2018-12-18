package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtTransactionalAccountParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.IbanIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngAtTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger logger = LoggerFactory.getLogger(IngAtTransactionalAccountFetcher.class);
    private IngAtApiClient apiClient;
    private IngAtSessionStorage sessionStorage;

    public IngAtTransactionalAccountFetcher(final IngAtApiClient apiClient,
            final IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private static boolean isTransactionalAccountType(AccountReferenceEntity r) {
        final String type = r.getType();
        switch (type.toUpperCase()) {
            case "CHECKING":
            case "SAVINGS":
                return true;
        }
        logger.warn("Unknown account type: {}", type);
        return false;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final WebLoginResponse webLoginResponse = sessionStorage.getWebLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Could not find login response when fetching accounts"));
        final List<AccountReferenceEntity> transactionalAccountReferences = webLoginResponse
                .getAccountReferenceEntities().stream()
                .filter(r -> isTransactionalAccountType(r)).collect(Collectors.toList());
        final Collection<TransactionalAccount> res = new ArrayList<>();
        for (AccountReferenceEntity r : transactionalAccountReferences) {
            final HttpResponse response = apiClient.getAccountDetails(new URL(r.getUrl()));
            final IngAtTransactionalAccountParser parser = new IngAtTransactionalAccountParser(
                    response.getBody(String.class));
            final IbanIdentifier ibanId = parser.getBic().isPresent() ? new IbanIdentifier(parser.getBic().get(), parser.getIban()) : new IbanIdentifier(parser.getIban());
            TransactionalAccount.Builder builder = TransactionalAccount.builder(
                    AccountTypes.valueOf(r.getType()),
                    r.getId(),
                    parser.getAmount());
            builder.setAccountNumber(r.getId())
                    .addIdentifier(ibanId);

            Optional.ofNullable(r.getAccountName()).ifPresent(builder::setName);

            Optional.ofNullable(webLoginResponse.getAccountHolder())
                    .ifPresent(holder -> builder.setHolderName(new HolderName(holder)));

            builder.putInTemporaryStorage(IngAtConstants.Storage.ACCOUNT_INDEX.name(), r.getAccountIndex());

            res.add(builder.build());
        }

        return res;
    }
}