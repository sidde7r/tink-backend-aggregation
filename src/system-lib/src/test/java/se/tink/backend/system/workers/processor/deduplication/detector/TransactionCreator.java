package se.tink.backend.system.workers.processor.deduplication.detector;

import java.util.Date;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;

public class TransactionCreator {
    public static Transaction create(String description,String originalDescription, double amount,
                                     double originalAmount, Date date, Date originalDate, String accountId, String credentialId) {
        Transaction transaction = new Transaction();

        transaction.setOriginalDescription(originalDescription);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setOriginalAmount(originalAmount);
        transaction.setOriginalDate(originalDate);
        transaction.setDate(date);
        transaction.setAccountId(accountId);
        transaction.setCredentialsId(credentialId);
        return transaction;
    }

    public static Transaction create(String description, double amount, Date date, String accountId) {
        Transaction transaction = new Transaction();

        transaction.setOriginalDescription(description);
        transaction.setDescription(transaction.getOriginalDescription());
        transaction.setOriginalAmount(amount);
        transaction.setAmount(transaction.getOriginalAmount());
        transaction.setOriginalDate(date);
        transaction.setDate(transaction.getOriginalDate());
        transaction.setAccountId(accountId);

        return transaction;
    }

    static Transaction create(String description, double amount, String date, String accountId, boolean isPending) {
        Transaction transaction = create(description, amount, DateUtils.parseDate(date), accountId);
        transaction.setPending(isPending);

        return transaction;
    }

    static Transaction create(String description, double amount, Account account, int daysToCertainDate) {
        String accountId = account != null ? account.getId() : UUIDUtils.toTinkUUID(UUID.randomUUID());
        Date certainDate = account != null && account.getCertainDate() != null ? account.getCertainDate() : new Date();
        return create(description,
                amount,
                DateUtils.addDays(certainDate, daysToCertainDate),
                accountId);
    }

    static Transaction create(String description, double amount, Account account, Date transactionDate, boolean pending) {
        String accountId = account != null ? account.getId() : UUIDUtils.toTinkUUID(UUID.randomUUID());
        return create(description,
                amount,
                transactionDate.toString(),
                accountId, pending);
    }

    static Transaction create(String description, double amount, Account account, int daysToCertainDate,
            boolean pending) {
        Transaction transaction = create(description, amount, account, daysToCertainDate);
        transaction.setPending(pending);

        return transaction;
    }
}
