package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.StatementPeriodsDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.SupplementaryAccountsItem;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

@RequiredArgsConstructor
public class AmexCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final AmexApiClient amexApiClient;
    private final HmacMultiTokenStorage hmacMultiTokenStorage;
    private final HmacAccountIdStorage hmacAccountIdStorage;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final Map<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> accountsByToken =
                getAccounts();

        if (accountsByToken.isEmpty()) {
            return Collections.emptyList();
        }
        storeAccountIdWithToken(accountsByToken);

        Map<HmacToken, List<BalanceDto>> balanceByToken = mapBalances(accountsByToken);

        List<CreditCardAccount> accounts =
                accountsByToken.entrySet().stream()
                        .filter(entry -> isAccountActive(entry.getValue().getKey()))
                        .map(
                                entry ->
                                        createTransactionalAccount(
                                                entry.getKey(),
                                                entry.getValue().getLeft(),
                                                entry.getValue().getRight(),
                                                balanceByToken))
                        .collect(Collectors.toList());

        List<CreditCardAccount> subAccountList = fetchSubAccounts(accountsByToken, balanceByToken);

        accounts.addAll(subAccountList);

        return accounts;
    }

    private List<CreditCardAccount> fetchSubAccounts(
            Map<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> accountsByToken,
            Map<HmacToken, List<BalanceDto>> balanceMap) {

        return accountsByToken.entrySet().stream()
                .filter(entry -> isAccountActive(entry.getValue().getLeft()))
                .filter(entry -> entry.getValue().getLeft().haveSupplementaryAccounts())
                .map(
                        entry ->
                                entry.getValue()
                                        .getLeft()
                                        .toSubCreditCardAccount(
                                                entry.getValue().getRight(),
                                                balanceMap.get(entry.getKey())))
                .findFirst()
                .orElse(Collections.emptyList());
    }

    private CreditCardAccount createTransactionalAccount(
            HmacToken hmacToken,
            AccountsResponseDto accountsResponse,
            StatementPeriodsDto statementPeriods,
            Map<HmacToken, List<BalanceDto>> balanceMap) {
        // only basic card (not supplementary) has balance
        final List<BalanceDto> balances =
                accountsResponse.getIdentifiers().isBasic()
                        ? balanceMap.get(hmacToken)
                        : Collections.emptyList();

        String currencyCode =
                balances.stream()
                        .findFirst()
                        .map(BalanceDto::getIsoAlphaCurrencyCode)
                        .orElse(accountsResponse.getHolder().getCurrencyCode());

        return accountsResponse.toCreditCardAccount(balances, statementPeriods, currencyCode);
    }

    private Map<HmacToken, List<BalanceDto>> mapBalances(
            Map<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> hmacTokenPairMap) {
        return hmacTokenPairMap.entrySet().stream()
                .filter(
                        hmacTokenPairEntry ->
                                isAccountBasic(hmacTokenPairEntry.getValue().getKey()))
                .map(Entry::getKey)
                .map(token -> Pair.of(token, amexApiClient.fetchBalances(token)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Map<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> getAccounts() {
        final HmacMultiToken hmacMultiToken = getHmacMultiToken();

        return hmacMultiToken.getTokens().stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(), this::fetchAccountsAndStatementPeriods));
    }

    private Pair<AccountsResponseDto, StatementPeriodsDto> fetchAccountsAndStatementPeriods(
            HmacToken hmacToken) {
        return Pair.of(
                amexApiClient.fetchAccounts(hmacToken),
                amexApiClient.fetchStatementPeriods(hmacToken));
    }

    private HmacMultiToken getHmacMultiToken() {
        return hmacMultiTokenStorage
                .getToken()
                .orElseThrow(() -> new IllegalArgumentException("Hmac token was not found."));
    }

    /*
    Each main-account (including the corresponding sub-accounts) needs a unique token for fetching data.
    This function will create hashmaps for each main-account (and sub-accounts) that maps each account
    to a token.

    The function will then collect all hashmaps and merge them before returning.
     */
    private void storeAccountIdWithToken(
            Map<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> accountsByToken) {
        HmacAccountIds hmacAccountIds =
                new HmacAccountIds(
                        accountsByToken.entrySet().stream()
                                .map(this::mapTokenToAccountAndSubAccount)
                                .collect(Collectors.toSet())
                                .stream()
                                .flatMap(e -> e.entrySet().stream())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        hmacAccountIdStorage.store(hmacAccountIds);
    }

    private Map<String, HmacToken> mapTokenToAccountAndSubAccount(
            Map.Entry<HmacToken, Pair<AccountsResponseDto, StatementPeriodsDto>> entry) {
        Map<String, HmacToken> tokensByAccountId = new HashMap<>();

        final AccountsResponseDto account = entry.getValue().getLeft();

        // Add mainAccount and HmacToken to map
        tokensByAccountId.put(getAccountId(account), entry.getKey());

        // add subAccount and HmacToken to map if non null.
        if (account.getSupplementaryAccounts() != null) {
            tokensByAccountId.putAll(
                    createHmacSubAccountsId(account.getSupplementaryAccounts(), entry.getKey()));
        }

        return tokensByAccountId;
    }

    private Map<String, HmacToken> createHmacSubAccountsId(
            List<SupplementaryAccountsItem> supplementaryAccounts, HmacToken hmacToken) {
        return supplementaryAccounts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(this::getSubAccountId, t -> hmacToken));
    }

    private String getSubAccountId(SupplementaryAccountsItem supplementaryAccountsItem) {
        return supplementaryAccountsItem.getIdentifiers().getDisplayAccountNumber();
    }

    private String getAccountId(AccountsResponseDto accountsResponse) {
        return accountsResponse.getIdentifiers().getDisplayAccountNumber();
    }

    private static boolean isAccountActive(AccountsResponseDto accountsResponse) {
        return accountsResponse.getStatus().getAccountStatus().contains("Active");
    }

    private static boolean isAccountBasic(AccountsResponseDto accountsResponse) {
        return accountsResponse.getIdentifiers().isBasic();
    }
}
