package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.MemberEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.SettingsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.UserSettingsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class LunarAccountHoldersFetcher {

    private static final String DA_MAIN_USER_ALIAS = "dig";
    private static final String EN_MAIN_USER_ALIAS = "you";

    private final FetcherApiClient apiClient;
    private final Map<String, List<String>> accountsHolderNames;

    public String fetchAccountsHolders(AccountsResponse accountsResponse) {
        // Delete unnecessary logs after getting more data
        // If user has only not shared accounts, we should use the name on one of the cards.
        // In case of shared account, we should fetch members of the account. Members list does not
        // contain main account holder name, hence we should filter names from cards to get main
        // account holder.
        List<AccountEntity> accounts =
                accountsResponse.getAccounts().stream()
                        .filter(AccountEntity::isLunarAccount)
                        .filter(BaseResponseEntity::notDeleted)
                        .collect(Collectors.toList());
        List<String> accountsHoldersNamesFromCards = collectAccountsHoldersFromCards(accounts);
        String userId = fetchUserId();
        List<String> accountsMembers = collectAccountsMembers(accounts, userId);
        List<String> accountHolders =
                getMainAccountHolders(accountsHoldersNamesFromCards, accountsMembers);

        if (accountHolders.isEmpty()) {
            log.info("Couldn't find Lunar accountHolder!");
        } else if (accountHolders.size() > 1) {
            log.info("There is more than one Lunar accountHolder!");
        }

        // Return empty String to indicate that account holders were already fetched
        return accountsHoldersNamesFromCards.isEmpty() ? "" : accountsHoldersNamesFromCards.get(0);
    }

    private List<String> collectAccountsHoldersFromCards(List<AccountEntity> accounts) {
        return accounts.stream()
                .flatMap(account -> fetchAccountHoldersNamesFromCards(account).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> fetchAccountHoldersNamesFromCards(AccountEntity account) {
        try {
            return apiClient.fetchCardsByAccount(account.getId()).getCards().stream()
                    .filter(CardEntity::isHolderNameNotBlank)
                    .map(CardEntity::getCardholderName)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            log.info("Failed to fetch cards for Lunar account!", e);
            return Collections.emptyList();
        }
    }

    private String fetchUserId() {
        return Optional.ofNullable(apiClient.getUserSettings())
                .map(UserSettingsResponse::getSettings)
                .map(SettingsEntity::getId)
                .orElse(null);
    }

    private List<String> collectAccountsMembers(List<AccountEntity> accounts, String userId) {
        return accounts.stream()
                .filter(account -> BooleanUtils.isTrue(account.getIsShared()))
                .flatMap(account -> fetchAccountMembers(account, userId).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getMainAccountHolders(
            List<String> accountsHoldersNamesFromCards, List<String> accountsMembers) {
        // Filter account holder names from cards to check what name is not among members.
        // It should return only one name of main account holder.
        return accountsHoldersNamesFromCards.stream()
                .filter(accountHolderFromCard -> !accountsMembers.contains(accountHolderFromCard))
                .collect(Collectors.toList());
    }

    private List<String> fetchAccountMembers(AccountEntity account, String userId) {
        // Account members do not contain real name of main account holder, but only "Dig"/"You"
        // Try to filter main account holder by id from settings. I am not sure if it will work,
        // hence additional check for user name. With more information from logs, delete these
        // comments and correct the logic.
        log.info("Lunar account is shared");
        try {
            List<String> accountMembers =
                    apiClient.fetchMembers(account.getId()).getMembers().stream()
                            .filter(member -> !isMainAccountHolder(userId, member))
                            .map(MemberEntity::getName)
                            .collect(Collectors.toList());
            accountsHolderNames.put(account.getId(), accountMembers);
            return accountMembers;
        } catch (HttpResponseException e) {
            log.info("Failed to fetch account members!", e);
            return Collections.emptyList();
        }
    }

    private boolean isMainAccountHolder(String userId, MemberEntity member) {
        return (userId != null && !userId.equals(member.getUserId()))
                || DA_MAIN_USER_ALIAS.equalsIgnoreCase(member.getName())
                || EN_MAIN_USER_ALIAS.equalsIgnoreCase(member.getName());
    }
}
