package se.tink.backend.aggregation.agents.framework.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AgentContractEntitiesAsserts {

    private static <T> boolean areEqualIgnoringOrder(
            List<T> list1, List<T> list2, EqualityChecker comparator) {

        if (list1.size() != list2.size()) {
            return false;
        }

        List<T> copy1 = new ArrayList<>(list1);
        List<T> copy2 = new ArrayList<>(list2);

        for (T obj1 : copy1) {
            boolean found = false;
            for (T obj2 : copy2) {
                if (comparator.isEqual(obj1, obj2)) {
                    found = true;
                    copy2.remove(obj2);
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    public static void compareAccounts(List<Account> expected, List<Account> given) {
        Assert.assertTrue(
                areEqualIgnoringOrder(
                        expected,
                        given,
                        new EqualityChecker<Account>() {
                            @Override
                            public boolean isEqual(Account account1, Account account2) {
                                return Account.deepEquals(account1, account2);
                            }
                        }));
    }

    public static void compareTransactions(List<Transaction> expected, List<Transaction> given) {
        Assert.assertTrue(
                areEqualIgnoringOrder(
                        expected,
                        given,
                        new EqualityChecker<Transaction>() {
                            @Override
                            public boolean isEqual(
                                    Transaction transaction1, Transaction transaction2) {
                                return Double.compare(
                                                        transaction1.getAmount(),
                                                        transaction2.getAmount())
                                                == 0
                                        && Double.compare(
                                                        transaction1.getOriginalAmount(),
                                                        transaction2.getOriginalAmount())
                                                == 0
                                        && transaction1.isPending() == transaction2.isPending()
                                        && transaction1.isUpcoming() == transaction2.isUpcoming()
                                        && Objects.equals(
                                                transaction1.getCredentialsId(),
                                                transaction2.getCredentialsId())
                                        && Objects.equals(
                                                transaction1.getDate(), transaction2.getDate())
                                        && Objects.equals(
                                                transaction1.getDescription(),
                                                transaction2.getDescription())
                                        && transaction1.getType() == transaction2.getType()
                                        && Objects.equals(
                                                transaction1.getUserId(), transaction2.getUserId());
                            }
                        }));
    }
}
