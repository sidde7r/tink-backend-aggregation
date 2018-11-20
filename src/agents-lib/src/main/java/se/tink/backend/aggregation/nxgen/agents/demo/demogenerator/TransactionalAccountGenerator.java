package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoSavingsAccountDefinition;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoTransactionAccountDefinition;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.i18n.Catalog;

public class TransactionalAccountGenerator {
    public static Collection<TransactionalAccount> fetchTransactionalAccounts(String currency, Catalog catalog,
            DemoTransactionAccountDefinition transactionAccountDefinition,
            DemoSavingsAccountDefinition savingsAccountDefinition) {
        ArrayList<TransactionalAccount> accounts = Lists.newArrayList();

        accounts.add(TransactionalAccount.builder(AccountTypes.CHECKING,
                transactionAccountDefinition.getAccountId(), new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, transactionAccountDefinition.getBalance())))
                .setAccountNumber(transactionAccountDefinition.getAccountId())
                .setName(catalog.getString(transactionAccountDefinition.getAccountName()))
                .setBankIdentifier(transactionAccountDefinition.getAccountId())
                .build());

        accounts.add(TransactionalAccount.builder(AccountTypes.SAVINGS,
                savingsAccountDefinition.getAccountId(), new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, savingsAccountDefinition.getAccountBalance())))
                .setAccountNumber(savingsAccountDefinition.getAccountId())
                .setName(catalog.getString(savingsAccountDefinition.getAccountName()))
                .setBankIdentifier(savingsAccountDefinition.getAccountId())
                .build());

        return accounts;
    }
}
