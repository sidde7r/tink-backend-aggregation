package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.i18n.Catalog;

public class DemoAccountFactory {
    public static Collection<TransactionalAccount> fetchTransactionalAccounts(String currency, Catalog catalog,
            DemoTransactionAccount transactionAccountDefinition,
            DemoSavingsAccount savingsAccountDefinition) {
        ArrayList<TransactionalAccount> accounts = Lists.newArrayList();

        if (Objects.nonNull(transactionAccountDefinition)) {
            accounts.add(TransactionalAccount.builder(AccountTypes.CHECKING,
                    transactionAccountDefinition.getAccountId(), new Amount(currency,
                            DemoConstants.getSekToCurrencyConverter(currency, transactionAccountDefinition.getBalance())))
                    .setAccountNumber(transactionAccountDefinition.getAccountId())
                    .setName(catalog.getString(transactionAccountDefinition.getAccountName()))
                    .setBankIdentifier(transactionAccountDefinition.getAccountId())
                    .build());
        }

        if (Objects.nonNull(savingsAccountDefinition)) {
            accounts.add(TransactionalAccount.builder(AccountTypes.SAVINGS,
                    savingsAccountDefinition.getAccountId(), new Amount(currency,
                            DemoConstants.getSekToCurrencyConverter(currency, savingsAccountDefinition.getAccountBalance())))
                    .setAccountNumber(savingsAccountDefinition.getAccountId())
                    .setName(catalog.getString(savingsAccountDefinition.getAccountName()))
                    .setBankIdentifier(savingsAccountDefinition.getAccountId())
                    .build());
        }

        return accounts;
    }

  public static Collection<CreditCardAccount> createCreditCardAccounts(
      Catalog catalog, DemoCreditCardAccount demoCreditCardAccountDefinition) {
    return Collections.singleton(
        CreditCardAccount.builderFromFullNumber(
                demoCreditCardAccountDefinition.getCreditCardNumber())
            .setName(
                    catalog.getString(demoCreditCardAccountDefinition.getAccountName()))
            .setHolderName(
                    demoCreditCardAccountDefinition.getNameOnCreditCard())
            .setBalance(
                    demoCreditCardAccountDefinition.getBalance())
            .setAvailableCredit(
                    demoCreditCardAccountDefinition.getAvailableCredit())
            .build());
  }
}
