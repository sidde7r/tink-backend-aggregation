package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.Account;

/**
 * In order to construct a complete Tink account, data needs to be combined from a Account and
 * Balance request.
 *
 * @param <AccountResponse> Ukob Account entity
 * @param <BalanceResponse> Ukob Balance entity
 * @param <AccountType> Resulting Tink account type
 * @see <a href="https://openbanking.atlassian.net/wiki/spaces/DZ/pages/129040604/Accounts+v2.0.0">
 *     Ukob Account documentation</a>
 * @see <a href="https://openbanking.atlassian.net/wiki/spaces/DZ/pages/128909480/Balances+v2.0.0">
 *     Ukob Balance documentation</a>
 */
public interface AccountConverter<AccountResponse, BalanceResponse, AccountType extends Account> {

    Optional<? extends AccountType> toTinkAccount(
            AccountResponse accountResponse, BalanceResponse balanceResponse);
}
