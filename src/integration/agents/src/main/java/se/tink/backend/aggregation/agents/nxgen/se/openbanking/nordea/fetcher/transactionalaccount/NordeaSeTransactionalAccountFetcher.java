package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;

public class NordeaSeTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        extends NordeaBaseTransactionalAccountFetcher<R>
        implements AccountFetcher<TransactionalAccount> {

    NordeaBaseApiClient apiClient;

    public NordeaSeTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient, Class<R> transactionResponseClass) {
        super(apiClient, transactionResponseClass);
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse accountsResponse = apiClient.getAccounts();
        return Optional.ofNullable(accountsResponse.getResponse().getAccounts())
                .orElse(Collections.emptyList()).stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        AccountIdentifier identifier = accountEntity.generalGetAccountIdentifier();
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                        accountEntity.getAccountType(),
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(accountEntity.getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getLast4Bban())
                                .withAccountNumber(identifier.getIdentifier())
                                .withAccountName(accountEntity.getProduct())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                accountEntity.getIban()))
                                .addIdentifier(identifier)
                                .build())
                .putInTemporaryStorage(
                        NordeaBaseConstants.StorageKeys.ACCOUNT_ID, accountEntity.getId())
                .setApiIdentifier(accountEntity.getId())
                .addHolderName(accountEntity.getHolderName())
                .build();
    }
}
