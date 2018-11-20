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

    private static final String CHECKING_ACCOUNT_ID = "9999-111111111111";
    private static final double CHECKING_ACCOUNT_BALANCE = 26245.33;
    private static final String CHECKING_ACCOUNT_NAME = "Debt Account";

    private static final String SAVINGS_ACCOUNT_ID = "9999-222222222222";
    private static final double SAVINGS_ACCOUNT_BALANCE = 385245.33;
    private static final String SAVINGS_ACCOUNT_NAME = "Savings Account";

    public static Collection<TransactionalAccount> fetchTransactionalAccounts(String currency, Catalog catalog) {
        ArrayList<TransactionalAccount> accounts = Lists.newArrayList();

        accounts.add(TransactionalAccount.builder(AccountTypes.CHECKING,
                CHECKING_ACCOUNT_ID, new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, CHECKING_ACCOUNT_BALANCE)))
                .setAccountNumber(CHECKING_ACCOUNT_ID)
                .setName(catalog.getString(CHECKING_ACCOUNT_NAME))
                .setBankIdentifier(CHECKING_ACCOUNT_ID)
                .build());

        accounts.add(TransactionalAccount.builder(AccountTypes.CREDIT_CARD,
                SAVINGS_ACCOUNT_ID, new Amount(currency,
                        DemoConstants.getSekToCurrencyConverter(currency, SAVINGS_ACCOUNT_BALANCE)))
                .setAccountNumber(SAVINGS_ACCOUNT_ID)
                .setName(catalog.getString(SAVINGS_ACCOUNT_NAME))
                .setBankIdentifier(SAVINGS_ACCOUNT_ID)
                .build());

        return accounts;
    }
}
