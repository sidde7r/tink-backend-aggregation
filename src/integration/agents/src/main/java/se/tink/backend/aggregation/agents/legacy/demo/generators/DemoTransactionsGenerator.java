package se.tink.backend.aggregation.agents.demo.generators;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.credentials.demo.DemoCredentials;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DemoTransactionsGenerator {

    public static List<Transaction> generateTransactions(DemoCredentials demoCredentials, Account account) {
        // Support is only implemented for User13.
        if (Objects.equals(demoCredentials.getUsername(), DemoCredentials.USER13.getUsername())) {
            return generateIdentityEventTransactions();
        }

        return Collections.emptyList();
    }

    private static List<Transaction> generateIdentityEventTransactions() {
        List<Transaction> transactions = Lists.newArrayList();

        // Generate a baseline with one transaction per day for the last 100 days
        for (int i = 0; i < 100; i++) {
            transactions.add(createTransaction(DateTime.now().minusDays(i), UUIDUtils.generateUUID(), -10D));
        }

        // Generate transactions for a double charge event
        transactions.add(createTransaction(DateTime.now(), "H&M Stockholm", -2000D));
        transactions.add(createTransaction(DateTime.now(), "H&M Stockholm", -2000D));

        // Generate transactions on the same day to trigger a frequent account activity event
        for (int i = 0; i < 10; i++) {
            transactions.add(createTransaction(DateTime.now().minusDays(1),
                    String.format("Frequent account activity transaction: %d", i), -100D));
        }

        return transactions;
    }

    private static Transaction createTransaction(DateTime date, String description, Double amount) {
        Transaction transaction = new Transaction();
        transaction.setDate(DateUtils.flattenTime(date.toDate()));
        transaction.setDescription(description);
        transaction.setAmount(amount);
        return transaction;
    }
}
