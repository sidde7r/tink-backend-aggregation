package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.fetcher.transactionalaccount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.AccountDetailsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NordeaSeTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        extends NordeaBaseTransactionalAccountFetcher<R>
        implements AccountFetcher<TransactionalAccount> {

    NordeaBaseApiClient apiClient;

    public NordeaSeTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient,
            Class<R> transactionResponseClass,
            String providerMarket) {
        super(apiClient, transactionResponseClass, providerMarket);
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
        AccountDetailsResponseEntity detailsResponseEntity =
                apiClient.getAccountDetails(accountEntity.getId());
        TransactionalBuildStep account =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                                accountEntity.getAccountType())
                        .withBalance(getBalanceModule(detailsResponseEntity))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountEntity.getLast4Bban())
                                        .withAccountNumber(identifier.getIdentifier())
                                        .withAccountName(accountEntity.getProduct())
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN,
                                                        accountEntity.getIban()))
                                        .addIdentifier(identifier)
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID, accountEntity.getId())
                        .setApiIdentifier(accountEntity.getId());

        account.addHolderName(apiClient.getAccountDetails(accountEntity.getId()).getAccountName());

        return account.build();
    }

    private BalanceModule getBalanceModule(AccountDetailsResponseEntity entity) {
        return BalanceModule.builder()
                .withBalance(
                        new ExactCurrencyAmount(
                                BigDecimal.valueOf(entity.getBookedBalance()),
                                entity.getCurrency()))
                .setAvailableBalance(
                        new ExactCurrencyAmount(
                                BigDecimal.valueOf(entity.getAvailableBalance()),
                                entity.getCurrency()))
                .build();
    }
}
