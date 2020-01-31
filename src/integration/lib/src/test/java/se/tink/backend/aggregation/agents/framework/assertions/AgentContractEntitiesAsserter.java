package se.tink.backend.aggregation.agents.framework.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AccountTestEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.TransactionTestEntity;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AgentContractEntitiesAsserter {

    public void compareAccounts(List<Account> expected, List<Account> given) {
        List<AccountTestEntity> expectedAccounts =
                expected.stream().map(a -> new AccountTestEntity(a)).collect(Collectors.toList());

        List<AccountTestEntity> givenAccounts =
                given.stream().map(a -> new AccountTestEntity(a)).collect(Collectors.toList());

        Collections.sort(expectedAccounts);
        Collections.sort(givenAccounts);

        assertThat(expectedAccounts).isEqualTo(givenAccounts);
    }

    public void compareTransactions(List<Transaction> expected, List<Transaction> given) {
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
}
