package se.tink.backend.common.statistics.functions;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;

/**
 * Transforms an account balance history entry into its account type group.
 */
public class AccountBalanceToAccountTypeFunction implements Function<AccountBalance, String> {

    final public static String CARDS_AND_ACCOUNTS = "cards-and-accounts";
    final public static String LOANS = "loans";
    final public static String SAVINGS = "savings";

    private final Map<UUID, Account> accountsById;

    public AccountBalanceToAccountTypeFunction(List<Account> accounts) {
        accountsById = accounts.stream().collect(Collectors.toMap(
                a -> UUIDUtils.fromTinkUUID(a.getId()),
                Function.identity()));
    }

    public String apply(AccountBalance accountBalance) {
        switch (accountsById.get(accountBalance.getAccountId()).getType()) {
        case CHECKING:
        case CREDIT_CARD:
        case OTHER:
            return CARDS_AND_ACCOUNTS;

        case LOAN:
        case MORTGAGE:
            return LOANS;

        case INVESTMENT:
        case PENSION:
        case SAVINGS:
        default:
            return SAVINGS;
        }
    }

}

