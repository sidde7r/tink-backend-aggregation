package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.StatementDto;
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
        final Map<HmacToken, AccountsResponseDto> accountsByToken = getAccounts();

        if (accountsByToken.isEmpty()) {
            return Collections.emptyList();
        }
        storeAccountIdWithToken(accountsByToken);

        List<CreditCardAccount> accounts =
                accountsByToken.entrySet().stream()
                        .filter(entry -> isAccountActive(entry.getValue()))
                        .map(entry -> createTransactionalAccount(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

        List<CreditCardAccount> subAccountList = fetchSubAccounts(accountsByToken);

        accounts.addAll(subAccountList);

        return accounts;
    }

    private List<CreditCardAccount> fetchSubAccounts(
            Map<HmacToken, AccountsResponseDto> accountsByToken) {

        return accountsByToken.values().stream()
                .filter(AmexCreditCardFetcher::isAccountActive)
                .filter(AccountsResponseDto::haveSupplementaryAccounts)
                .map(AccountsResponseDto::toSubCreditCardAccount)
                .findFirst()
                .orElse(Collections.emptyList());
    }

    private CreditCardAccount createTransactionalAccount(
            HmacToken hmacToken, AccountsResponseDto accountsResponse) {

        final List<BalanceDto> balances = getBalances(hmacToken);
        final Map<Integer, String> statementMap =
                getStatementPeriods(hmacToken).getStatementPeriods().stream()
                        .collect(
                                Collectors.toMap(
                                        StatementDto::getIndex, StatementDto::getEndDateAsString));

        return accountsResponse.toCreditCardAccount(balances, statementMap);
    }

    private List<BalanceDto> getBalances(HmacToken hmacToken) {
        return amexApiClient.fetchBalances(hmacToken);
    }

    private StatementPeriodsDto getStatementPeriods(HmacToken hmacToken) {
        return amexApiClient.fetchStatementPeriods(hmacToken);
    }

    private Map<HmacToken, AccountsResponseDto> getAccounts() {
        final HmacMultiToken hmacMultiToken = getHmacMultiToken();

        return hmacMultiToken.getTokens().stream()
                .collect(Collectors.toMap(Function.identity(), amexApiClient::fetchAccounts));
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
    private void storeAccountIdWithToken(Map<HmacToken, AccountsResponseDto> accountsByToken) {
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
            Map.Entry<HmacToken, AccountsResponseDto> entry) {
        Map<String, HmacToken> tokensByAccountId = new HashMap<>();
        // Add mainAccount and HmacToken to map
        tokensByAccountId.put(getAccountId(entry.getValue()), entry.getKey());

        // add subAccount and HmacToken to map if non null.
        if (entry.getValue().getSupplementaryAccounts() != null) {
            tokensByAccountId.putAll(
                    createHmacSubAccountsId(
                            entry.getValue().getSupplementaryAccounts(), entry.getKey()));
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
        return AmericanExpressUtils.formatAccountId(
                supplementaryAccountsItem.getIdentifiers().getDisplayAccountNumber());
    }

    private String getAccountId(AccountsResponseDto accountsResponse) {
        return AmericanExpressUtils.formatAccountId(
                accountsResponse.getIdentifiers().getDisplayAccountNumber());
    }

    private static boolean isAccountActive(AccountsResponseDto accountsResponse) {
        return accountsResponse.getStatus().getAccountStatus().contains("Active");
    }
}
