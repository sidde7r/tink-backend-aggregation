package se.tink.backend.aggregation.agents.contexts;

import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.i18n.Catalog;

public interface StatusUpdater {
    void updateStatus(CredentialsStatus status);

    void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider);

    Map<String, Integer> getTransactionCountByEnabledAccount();

    Catalog getCatalog();

    default void updateStatus(CredentialsStatus status, String statusPayload) {
        updateStatus(status, statusPayload, true);
    }

    default void updateStatus(
            CredentialsStatus status, Account account, List<Transaction> transactions) {
        if (account.isExcluded()) {
            if (getTransactionCountByEnabledAccount().containsKey(account.getBankId())) {
                getTransactionCountByEnabledAccount().remove(account.getBankId());
            }
        } else {
            getTransactionCountByEnabledAccount().put(account.getBankId(), transactions.size());
        }

        updateStatus(status, createStatusPayload());
    }

    default String createStatusPayload() {
        Catalog catalog = getCatalog();

        int numberOfAccounts = getTransactionCountByEnabledAccount().size();
        int numberOfTransactions = 0;

        for (Integer accountTransactions : getTransactionCountByEnabledAccount().values()) {
            numberOfTransactions += accountTransactions;
        }

        return Catalog.format(
                catalog.getString("Updating {0}..."),
                formatCredentialsStatusPayloadSuffix(
                        numberOfAccounts, numberOfTransactions, catalog));
    }

    default String formatCredentialsStatusPayloadSuffix(
            long numberOfAccounts, long numberOfTransactions, Catalog catalog) {
        StringBuilder builder = new StringBuilder();

        builder.append(
                Catalog.format(
                        catalog.getPluralString("{0} account", "{0} accounts", numberOfAccounts),
                        numberOfAccounts));

        if (numberOfTransactions > 0) {
            builder.append(" ");
            builder.append(catalog.getString("and"));
            builder.append(" ");

            builder.append(
                    Catalog.format(
                            catalog.getPluralString(
                                    "{0} transaction", "{0} transactions", numberOfTransactions),
                            numberOfTransactions));
        }

        return builder.toString();
    }
}
