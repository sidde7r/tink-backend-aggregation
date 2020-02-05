package se.tink.backend.aggregation.agents.framework.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AccountTestEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.TransactionTestEntity;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AgentContractEntitiesAsserts {

    public static void compareAccounts(List<Account> expected, List<Account> given) {
        List<AccountTestEntity> expectedAccounts =
                expected.stream().map(a -> new AccountTestEntity(a)).collect(Collectors.toList());

        List<AccountTestEntity> givenAccounts =
                given.stream().map(a -> new AccountTestEntity(a)).collect(Collectors.toList());

        Collections.sort(expectedAccounts);
        Collections.sort(givenAccounts);

        assertThat(expectedAccounts).isEqualTo(givenAccounts);
    }

    public static void compareTransactions(List<Transaction> expected, List<Transaction> given) {
        List<TransactionTestEntity> expectedTransactions =
                expected.stream()
                        .map(a -> new TransactionTestEntity(a))
                        .collect(Collectors.toList());

        List<TransactionTestEntity> givenTransactions =
                given.stream().map(a -> new TransactionTestEntity(a)).collect(Collectors.toList());

        Collections.sort(expectedTransactions);
        Collections.sort(givenTransactions);

        assertThat(expectedTransactions).isEqualTo(givenTransactions);
    }

    public static Account createAccount(
            String accountNumber,
            double availableCredit,
            double balance,
            String currencyCode,
            String bankId,
            String name,
            AccountTypes accountType,
            String holderName) {

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountExclusion(AccountExclusion.NONE);
        account.setExactAvailableCredit(
                new ExactCurrencyAmount(new BigDecimal(availableCredit), currencyCode));
        account.setExactBalance(new ExactCurrencyAmount(new BigDecimal(balance), currencyCode));
        account.setCurrencyCode(currencyCode);
        account.setBankId(bankId);
        account.setExcluded(false);
        account.setFavored(false);
        account.setName(name);
        account.setOwnership(1.0);
        account.setType(accountType);
        account.setUserModifiedExcluded(false);
        account.setUserModifiedName(false);
        account.setUserModifiedType(false);
        account.setIdentifiers(new ArrayList<>());
        account.setClosed(false);
        account.setHolderName(holderName);
        account.setFlags(new ArrayList<>());
        account.setAvailableCredit(availableCredit);
        account.setBalance(balance);
        return account;
    }

    public static Transaction createTransaction(
            String accountId,
            double amount,
            long date,
            String description,
            boolean pending,
            boolean upcoming,
            TransactionTypes type) {

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setDate(new Date(date));
        transaction.setDescription(description);
        transaction.setPending(pending);
        transaction.setType(type);
        transaction.setUpcoming(upcoming);
        return transaction;
    }
}
