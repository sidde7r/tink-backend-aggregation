package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter.AmexTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

@RequiredArgsConstructor
public class AmexTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final AmexApiClient amexApiClient;
    private final HmacMultiTokenStorage hmacMultiTokenStorage;
    private final HmacAccountIdStorage hmacAccountIdStorage;
    private final AmexTransactionalAccountConverter amexTransactionalAccountConverter;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final Map<HmacToken, AccountsResponseDto> accountsByToken = getAccounts();

        if (accountsByToken.isEmpty()) {
            return Collections.emptyList();
        }

        hmacAccountIdStorage.store(createHmacAccountIds(accountsByToken));

        return accountsByToken.entrySet().stream()
                .filter(entry -> isAccountActive(entry.getValue()))
                .map(entry -> createTransactionalAccount(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> createTransactionalAccount(
            HmacToken hmacToken, AccountsResponseDto accountsResponse) {
        final List<BalanceDto> balances = getBalances(hmacToken);

        return amexTransactionalAccountConverter.toTransactionalAccount(accountsResponse, balances);
    }

    private Map<HmacToken, AccountsResponseDto> getAccounts() {
        final HmacMultiToken hmacMultiToken = getHmacMultiToken();

        return hmacMultiToken.getTokens().stream()
                .collect(Collectors.toMap(Function.identity(), amexApiClient::fetchAccounts));
    }

    private List<BalanceDto> getBalances(HmacToken hmacToken) {
        return amexApiClient.fetchBalances(hmacToken);
    }

    private HmacMultiToken getHmacMultiToken() {
        return hmacMultiTokenStorage
                .getToken()
                .orElseThrow(() -> new IllegalArgumentException("Hmac token was not found."));
    }

    private HmacAccountIds createHmacAccountIds(
            Map<HmacToken, AccountsResponseDto> accountsByToken) {
        final Map<String, HmacToken> tokensByAccountId =
                accountsByToken.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        entry -> getAccountId(entry.getValue()),
                                        Map.Entry::getKey));

        return new HmacAccountIds(tokensByAccountId);
    }

    private String getAccountId(AccountsResponseDto accountsResponse) {
        return accountsResponse.getIdentifiers().getDisplayAccountNumber();
    }

    private static boolean isAccountActive(AccountsResponseDto accountsResponse) {
        return accountsResponse.getStatus().getAccountStatus().contains("Active");
    }
}
