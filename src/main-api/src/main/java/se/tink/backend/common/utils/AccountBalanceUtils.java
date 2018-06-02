package se.tink.backend.common.utils;

import java.util.Date;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class AccountBalanceUtils {

    public static AccountBalance createEntry(Account account) {
        return createEntry(account, System.currentTimeMillis());
    }
     
    public static AccountBalance createEntry(Account account, long inserted) {
        return createEntry(account.getUserId(), account.getId(), DateUtils.getDateFromTimestamp(inserted),
                account.getBalance(), inserted);
    }

    public static AccountBalance createEntry(String userId, String accountId, Date date, double balance, long inserted) {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountId(UUIDUtils.fromTinkUUID(accountId));
        accountBalance.setBalance(balance);
        accountBalance.setDate(DateUtils.toInteger(date));
        accountBalance.setInserted(inserted);
        accountBalance.setUserId(UUIDUtils.fromTinkUUID(userId));
        return accountBalance;
    }
}
