package se.tink.backend.insights.accounts.mapper;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.insights.core.valueobjects.Account;
import se.tink.backend.insights.core.valueobjects.AccountId;
import se.tink.backend.insights.core.valueobjects.Balance;

public class AccountMapper {
    public static Account translate(se.tink.backend.core.Account account) {
        return Account.of(
                account.getName(),
                AccountId.of(account.getId()),
                Balance.of(account.getBalance()),
                account.getType()
        );
    }

    public static List<Account> translate(List<se.tink.backend.core.Account> accounts) {
        return accounts.stream().map(AccountMapper::translate).collect(Collectors.toList());
    }
}
