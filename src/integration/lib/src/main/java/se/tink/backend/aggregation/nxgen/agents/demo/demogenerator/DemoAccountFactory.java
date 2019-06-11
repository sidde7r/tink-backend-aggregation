package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.i18n.Catalog;

public class DemoAccountFactory {
    public static Collection<TransactionalAccount> fetchTransactionalAccounts(
            String currency,
            Catalog catalog,
            List<DemoTransactionAccount> transactionAccountDefinition,
            DemoSavingsAccount savingsAccountDefinition) {
        ArrayList<TransactionalAccount> accounts = Lists.newArrayList();

        if (Objects.nonNull(transactionAccountDefinition)) {
            for (DemoTransactionAccount transactionAccount : transactionAccountDefinition) {
                accounts.add(
                        TransactionalAccount.builder(
                                        AccountTypes.CHECKING,
                                        transactionAccount.getAccountId(),
                                        new Amount(
                                                currency,
                                                DemoConstants.getSekToCurrencyConverter(
                                                        currency, transactionAccount.getBalance())))
                                .setAccountNumber(transactionAccount.getAccountId())
                                .setName(catalog.getString(transactionAccount.getAccountName()))
                                .addIdentifiers(transactionAccount.getIdentifiers())
                                .build());
            }
        }

        if (Objects.nonNull(savingsAccountDefinition)) {
            accounts.add(
                    TransactionalAccount.builder(
                                    AccountTypes.SAVINGS,
                                    savingsAccountDefinition.getAccountId(),
                                    new Amount(
                                            currency,
                                            DemoConstants.getSekToCurrencyConverter(
                                                    currency,
                                                    savingsAccountDefinition.getAccountBalance())))
                            .setAccountNumber(savingsAccountDefinition.getAccountId())
                            .setName(catalog.getString(savingsAccountDefinition.getAccountName()))
                            .addIdentifiers(savingsAccountDefinition.getIdentifiers())
                            .build());
        }

        return accounts;
    }

    public static List<CreditCardAccount> createCreditCardAccounts(
            String currency,
            Catalog catalog,
            Collection<DemoCreditCardAccount> demoCreditCardAccountDefinitions) {
        ImmutableList.Builder<CreditCardAccount> accountListBuilder = ImmutableList.builder();

        for (DemoCreditCardAccount accountDefinition : demoCreditCardAccountDefinitions) {
            accountListBuilder.add(createCreditCardAccount(currency, catalog, accountDefinition));
        }

        return accountListBuilder.build();
    }

    private static CreditCardAccount createCreditCardAccount(
            String currency, Catalog catalog, DemoCreditCardAccount accountDefinition) {
        return CreditCardAccount.builderFromFullNumber(accountDefinition.getCreditCardNumber())
                .setName(catalog.getString(accountDefinition.getAccountName()))
                .setHolderName(accountDefinition.getNameOnCreditCard())
                .setBalance(
                        new Amount(
                                currency,
                                DemoConstants.getSekToCurrencyConverter(
                                        currency, accountDefinition.getBalance())))
                .setAvailableCredit(
                        new Amount(
                                currency,
                                DemoConstants.getSekToCurrencyConverter(
                                        currency, accountDefinition.getAvailableCredit())))
                .build();
    }
}
