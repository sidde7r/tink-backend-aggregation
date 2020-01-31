package se.tink.backend.aggregation.agents.framework.assertions.entities;

import java.util.Objects;
import se.tink.backend.aggregation.agents.models.Transaction;

public class TransactionTestEntity implements Comparable<TransactionTestEntity> {

    private Transaction transaction;

    public TransactionTestEntity(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean equals(Object o) {

        final Transaction expected = ((TransactionTestEntity) o).transaction;

        return Double.compare(transaction.getAmount(), expected.getAmount()) == 0
                && Double.compare(transaction.getOriginalAmount(), expected.getOriginalAmount())
                        == 0
                && transaction.isPending() == expected.isPending()
                && transaction.isUpcoming() == expected.isUpcoming()
                && Objects.equals(transaction.getCredentialsId(), expected.getCredentialsId())
                && Objects.equals(transaction.getDate(), expected.getDate())
                && Objects.equals(transaction.getDescription(), expected.getDescription())
                && transaction.getType() == expected.getType()
                && Objects.equals(transaction.getUserId(), expected.getUserId());
    }

    @Override
    public int compareTo(TransactionTestEntity u) {
        return transaction.getDate().compareTo(u.transaction.getDate());
    }
}
