package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@Slf4j
public class LunarTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, IdentityDataFetcher {

    private final FetcherApiClient apiClient;
    private final LunarDataAccessorFactory accessorFactory;
    private final PersistentStorage persistentStorage;
    private final Map<String, List<String>> accountsHolderNames;
    private final LunarAccountHoldersFetcher accountHoldersFetcher;
    private LunarAuthData authData;
    private String accountHolder;

    public LunarTransactionalAccountFetcher(
            FetcherApiClient apiClient,
            LunarDataAccessorFactory accessorFactory,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.accessorFactory = accessorFactory;
        this.persistentStorage = persistentStorage;
        this.accountsHolderNames = new HashMap<>();
        this.accountHoldersFetcher = new LunarAccountHoldersFetcher(apiClient, accountsHolderNames);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        fetchHoldersIfAccountHolderIsNull();
        accounts.addAll(fetchCheckingAccounts());
        accounts.addAll(fetchSavingsAccounts());
        return accounts;
    }

    private void fetchHoldersIfAccountHolderIsNull() {
        if (accountHolder == null) {
            accountHolder =
                    accountHoldersFetcher.fetchAccountsHolders(getAccountsResponseFromStorage());
        }
    }

    private AccountsResponse getAccountsResponseFromStorage() {
        AccountsResponse accountsResponse = getLunarPersistedData().getAccountsResponse();
        if (accountsResponse == null) {
            throw new IllegalStateException("There is no Lunar accountsResponse in storage!");
        }
        return accountsResponse;
    }

    private Collection<TransactionalAccount> fetchCheckingAccounts() {
        return getAccountsResponseFromStorage().getAccounts().stream()
                .filter(AccountEntity::isLunarAccount)
                .filter(BaseResponseEntity::notDeleted)
                .map(this::toTransactionalAccountWithHoldersNames)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTransactionalAccountWithHoldersNames(
            AccountEntity account) {

        List<Party> holdersOfAnAccount = new ArrayList<>();
        if (BooleanUtils.isTrue(account.getIsShared())) {
            holdersOfAnAccount.addAll(getHoldersOfSharedAccount(account.getId()));
        } else if (StringUtils.isNotBlank(accountHolder)) {
            holdersOfAnAccount.add(new Party(accountHolder, Party.Role.HOLDER));
        }

        return account.toTransactionalAccount(holdersOfAnAccount);
    }

    private List<Party> getHoldersOfSharedAccount(String accountId) {
        if (StringUtils.isBlank(accountHolder)) {
            return Collections.emptyList();
        }
        List<Party> sharedAccountHolders = new ArrayList<>();
        sharedAccountHolders.add(new Party(accountHolder, Party.Role.HOLDER));
        sharedAccountHolders.addAll(
                accountsHolderNames.get(accountId).stream()
                        .map(name -> new Party(name, Party.Role.HOLDER))
                        .collect(Collectors.toList()));
        return sharedAccountHolders;
    }

    private Collection<TransactionalAccount> fetchSavingsAccounts() {
        return apiClient.fetchSavingGoals().getGoals().stream()
                .filter(BaseResponseEntity::notDeleted)
                .map(goal -> goal.toTransactionalAccount(getAccountHolderIfPresent()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<Party> getAccountHolderIfPresent() {
        return StringUtils.isBlank(accountHolder)
                ? Collections.emptyList()
                : Collections.singletonList(new Party(accountHolder, Party.Role.HOLDER));
    }

    private LunarAuthData getLunarPersistedData() {
        if (authData == null) {
            authData =
                    accessorFactory
                            .createAuthDataAccessor(
                                    new PersistentStorageService(persistentStorage)
                                            .readFromAgentPersistentStorage())
                            .get();
        }
        return authData;
    }

    @Override
    public IdentityData fetchIdentityData() {
        fetchHoldersIfAccountHolderIsNull();
        return IdentityData.builder()
                .setFullName(StringUtils.isBlank(accountHolder) ? null : accountHolder)
                .setDateOfBirth(null)
                .build();
    }
}
