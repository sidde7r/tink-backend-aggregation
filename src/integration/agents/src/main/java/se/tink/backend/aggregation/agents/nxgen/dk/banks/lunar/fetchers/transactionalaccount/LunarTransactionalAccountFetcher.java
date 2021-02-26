package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class LunarTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FetcherApiClient apiClient;
    private final LunarDataAccessorFactory accessorFactory;
    private final PersistentStorage persistentStorage;
    private final List<Party> accountParties;

    public LunarTransactionalAccountFetcher(
            FetcherApiClient apiClient,
            LunarDataAccessorFactory accessorFactory,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.accessorFactory = accessorFactory;
        this.persistentStorage = persistentStorage;
        this.accountParties = new ArrayList<>();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        accounts.addAll(fetchCheckingAccounts());
        accounts.addAll(fetchSavingsAccounts());
        return accounts;
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

    private AccountsResponse getAccountsResponseFromStorage() {
        AccountsResponse accountsResponse = getLunarPersistedData().get().getAccountsResponse();
        if (accountsResponse == null) {
            throw new IllegalStateException("There is no Lunar accountsResponse in storage!");
        }
        return accountsResponse;
    }

    private Optional<TransactionalAccount> toTransactionalAccountWithHoldersNames(
            AccountEntity account) {
        // Delete unnecessary logs after getting more data
        List<Party> holdersOfAnAccount = new ArrayList<>();
        try {
            holdersOfAnAccount =
                    apiClient.fetchCardsByAccount(account.getId()).getCards().stream()
                            .filter(CardEntity::isHolderNameNotBlank)
                            .map(CardEntity::getCardholderName)
                            .distinct()
                            .map(holderName -> new Party(holderName, Party.Role.HOLDER))
                            .collect(Collectors.toList());
            if (holdersOfAnAccount.isEmpty()) {
                log.info("Couldn't find holders for Lunar account!");
            }
            if (holdersOfAnAccount.size() > 1) {
                log.info("Lunar account has more than 1 holder based on cards!");
            }
        } catch (HttpResponseException e) {
            log.info("Failed to fetch cards for Lunar account!", e);
        }
        accountParties.addAll(holdersOfAnAccount);
        logMembersIfAccountIsShared(account);
        return account.toTransactionalAccount(holdersOfAnAccount);
    }

    private void logMembersIfAccountIsShared(AccountEntity account) {
        if (BooleanUtils.isTrue(account.getIsShared())) {
            log.info("Lunar account is shared!");
            try {
                apiClient.fetchMembers(account.getId());
            } catch (HttpResponseException e) {
                log.info("Failed to fetch account members", e);
            }
        }
    }

    private Collection<TransactionalAccount> fetchSavingsAccounts() {
        return apiClient.fetchSavingGoals().getGoals().stream()
                .filter(BaseResponseEntity::notDeleted)
                .map(goal -> goal.toTransactionalAccount(accountParties))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private LunarAuthDataAccessor getLunarPersistedData() {
        return accessorFactory.createAuthDataAccessor(
                new PersistentStorageService(persistentStorage).readFromAgentPersistentStorage());
    }
}
