package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

public class TransactionPageKey {
    private TransactionQuery query;
    private TransactionEntity lastTransaction;

    public TransactionPageKey(
            String customerNumber, String accountNumber, TransactionEntity lastTransaction) {
        this.query =
                new TransactionQuery(
                        customerNumber,
                        accountNumber,
                        80,
                        lastTransaction.getBookingDate(),
                        lastTransaction.getBatchNumber());
        this.lastTransaction = lastTransaction;
    }

    public TransactionEntity getLastTransaction() {
        return lastTransaction;
    }

    public TransactionQuery getQuery() {
        return query;
    }
}
