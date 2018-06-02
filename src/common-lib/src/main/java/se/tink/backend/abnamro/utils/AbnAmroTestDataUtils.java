package se.tink.backend.abnamro.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import se.tink.backend.common.utils.DemoDataUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;

public class AbnAmroTestDataUtils {

    private static final String BASE_PATH = "data/demo/abnamro/user1";
    private static final Joiner JOINER = Joiner.on(File.separator);

    public static List<Transaction> getTestTransactions(List<Account> accounts) {

        List<Transaction> transactions = Lists.newArrayList();

        for (Account account : accounts) {

            File file = new File(JOINER.join(BASE_PATH, account.getBankId() + ".txt"));

            try {
                transactions.addAll(DemoDataUtils.readTransactions(file, account, true));
            } catch (IOException e) {
                throw new RuntimeException("Could not read transaction file", e);
            }
        }

        // Add a dummy unique external id to all transactions.

        int externalId = 0;
        for (Transaction transaction : transactions) {
            transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, String.valueOf(externalId));
            externalId++;
        }

        return transactions;
    }

    public static List<Account> getTestAccounts(Credentials credentials) {

        try {
            File file = new File(JOINER.join(BASE_PATH, "accounts.txt"));

            return DemoDataUtils.readAccounts(file, credentials);
        } catch (IOException e) {
            throw new RuntimeException("Could not read account file", e);
        }
    }
}
