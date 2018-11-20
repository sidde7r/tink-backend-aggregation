package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.i18n.Catalog;

public class TransactionalAccountGenerator {
    public static Collection<TransactionalAccount> fetchTransactionalAccounts(String currency, Catalog catalog) {
        ArrayList<TransactionalAccount> accounts = Lists.newArrayList();

        accounts.add(TransactionalAccount.builder(AccountTypes.CHECKING,
                DemoConstants.CheckingAccountInformation.ACCOUNT_ID, new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, DemoConstants.CheckingAccountInformation.ACCOUNT_BALANCE)))
                .setAccountNumber(DemoConstants.CheckingAccountInformation.ACCOUNT_ID)
                .setName(catalog.getString(DemoConstants.CheckingAccountInformation.ACCOUNT_NAME))
                .setBankIdentifier(DemoConstants.CheckingAccountInformation.ACCOUNT_ID)
                .build());

        accounts.add(TransactionalAccount.builder(AccountTypes.SAVINGS,
                DemoConstants.SavingsAccountInformation.ACCOUNT_ID, new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, DemoConstants.SavingsAccountInformation.ACCOUNT_BALANCE)))
                .setAccountNumber(DemoConstants.SavingsAccountInformation.ACCOUNT_ID)
                .setName(catalog.getString(DemoConstants.SavingsAccountInformation.ACCOUNT_NAME))
                .setBankIdentifier(DemoConstants.SavingsAccountInformation.ACCOUNT_ID)
                .build());

        return accounts;
    }
}
