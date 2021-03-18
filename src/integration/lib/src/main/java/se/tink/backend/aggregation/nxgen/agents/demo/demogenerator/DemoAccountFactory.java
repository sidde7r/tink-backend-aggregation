package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
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
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withoutFlags()
                                .withBalance(
                                        getBalanceModule(
                                                currency,
                                                transactionAccount.getBalance(),
                                                transactionAccount.getCreditLimit(),
                                                transactionAccount.getAvailableBalance()))
                                .withId(
                                        getIdModule(
                                                transactionAccount.getIdentifiers(),
                                                transactionAccount.getAccountId(),
                                                catalog.getString(
                                                        transactionAccount.getAccountName())))
                                .build()
                                .get());
            }
        }

        if (Objects.nonNull(savingsAccountDefinition)) {
            accounts.add(
                    TransactionalAccount.nxBuilder()
                            .withType(TransactionalAccountType.SAVINGS)
                            .withoutFlags()
                            .withBalance(
                                    getBalanceModule(
                                            currency,
                                            savingsAccountDefinition.getAccountBalance(),
                                            savingsAccountDefinition.getCreditLimit(),
                                            savingsAccountDefinition.getAvailableBalance()))
                            .withId(
                                    getIdModule(
                                            savingsAccountDefinition.getIdentifiers(),
                                            savingsAccountDefinition.getAccountId(),
                                            catalog.getString(
                                                    savingsAccountDefinition.getAccountName())))
                            .build()
                            .get());
        }

        return accounts;
    }

    private static BalanceModule getBalanceModule(
            String currency,
            double accountBalance,
            Optional<Double> creditLimit,
            Optional<Double> availableBalance) {
        BalanceBuilderStep builder =
                BalanceModule.builder()
                        .withBalance(
                                ExactCurrencyAmount.of(
                                        DemoConstants.getSekToCurrencyConverter(
                                                currency, accountBalance),
                                        currency));
        creditLimit.ifPresent(
                limit ->
                        builder.setCreditLimit(
                                ExactCurrencyAmount.of(
                                        DemoConstants.getSekToCurrencyConverter(currency, limit),
                                        currency)));
        availableBalance.ifPresent(
                available ->
                        builder.setAvailableBalance(
                                ExactCurrencyAmount.of(
                                        DemoConstants.getSekToCurrencyConverter(
                                                currency, available),
                                        currency)));
        return builder.build();
    }

    private static IdModule getIdModule(
            List<AccountIdentifier> identifiers, String accountId, String accountName) {
        AccountIdentifier firstAccountIdentifier =
                identifiers.size() > 0 ? identifiers.get(0) : createDefaultAccountIdentifier();

        IdBuildStep idBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(accountId)
                        .withAccountNumber(accountId)
                        .withAccountName(accountName)
                        .addIdentifier(firstAccountIdentifier);

        if (identifiers.size() > 1) {
            // add the rest of identifiers
            for (int i = 1; i < identifiers.size(); i++) {
                idBuilder = idBuilder.addIdentifier(identifiers.get(i));
            }
        }
        return idBuilder.build();
    }

    private static AccountIdentifier createDefaultAccountIdentifier() {
        AccountIdentifier fakeIbanAccountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.TINK, UUID.randomUUID().toString());

        return fakeIbanAccountIdentifier;
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
                .setExactBalance(
                        ExactCurrencyAmount.of(
                                DemoConstants.getSekToCurrencyConverter(
                                        currency, accountDefinition.getBalance()),
                                currency))
                .setExactAvailableCredit(
                        ExactCurrencyAmount.of(
                                DemoConstants.getSekToCurrencyConverter(
                                        currency, accountDefinition.getAvailableCredit()),
                                currency))
                .build();
    }
}
