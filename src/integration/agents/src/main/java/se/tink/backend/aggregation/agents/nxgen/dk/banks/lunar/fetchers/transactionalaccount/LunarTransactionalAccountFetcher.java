package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class LunarTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FetcherApiClient apiClient;
    private List<Holder> accountsHolders = new ArrayList<>();

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        accounts.addAll(fetchCheckingAccounts());
        accounts.addAll(fetchSavingsAccounts());
        return accounts;
    }

    private Collection<TransactionalAccount> fetchCheckingAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(AccountEntity::isLunarAccount)
                .filter(BaseResponseEntity::notDeleted)
                .map(this::toTransactionalAccountWithHoldersNames)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTransactionalAccountWithHoldersNames(
            AccountEntity account) {
        // Delete unnecessary logs after getting more data
        List<Holder> holdersOfAnAccount = new ArrayList<>();
        try {
            holdersOfAnAccount =
                    apiClient.fetchCardsByAccount(account.getId()).getCards().stream()
                            .filter(CardEntity::isHolderNameNotBlank)
                            .map(CardEntity::getCardholderName)
                            .distinct()
                            .map(Holder::of)
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
        accountsHolders.addAll(holdersOfAnAccount);
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
                .map(goal -> goal.toTransactionalAccount(accountsHolders))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
