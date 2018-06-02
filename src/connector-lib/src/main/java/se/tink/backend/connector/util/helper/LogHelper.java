package se.tink.backend.connector.util.helper;

import com.google.common.base.MoreObjects;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

/**
 * The purpose of this class is to provide a general way to handle logging of objects, such as accounts or transactions.
 * This is to make sure that we don't log more than necessary and that it's done in a consistent way.
 */
public class LogHelper {

    public static String get(Account account) {
        return MoreObjects.toStringHelper(Account.class)
                .add("id", account.getId())
                .add("credentialsId", account.getCredentialsId())
                .add("userId", account.getUserId())
                .toString();
    }

    public static String get(Transaction transaction, CreateTransactionEntity entity, String externalAccountId) {
        return MoreObjects.toStringHelper(Transaction.class)
                .add("id", transaction.getId())
                .add("externalId", entity.getExternalId())
                .add("accountId", transaction.getAccountId())
                .add("externalAccountId", externalAccountId)
                .add("date", transaction.getDate())
                .toString();
    }

    public static String get(User user) {
        return MoreObjects.toStringHelper(User.class)
                .add("id", user.getId())
                .add("externalUserId", user.getUsername())
                .toString();
    }
}
