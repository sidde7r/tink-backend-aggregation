package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionMapper {

    static TransactionMapper getDefault() {
        return new DefaultTransactionMapper();
    }

    default Transaction toTinkTransaction(TransactionEntity transactionEntity) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionEntity.getSignedAmount())
                        .setDescription(transactionEntity.getTransactionInformation())
                        .setPending(transactionEntity.isPending())
                        .setMutable(transactionEntity.isMutable())
                        .setDate(transactionEntity.getDateOfTransaction())
                        .setTransactionDates(transactionEntity.getTransactionDates())
                        .setTransactionReference(transactionEntity.getTransactionReference())
                        .setProviderMarket(getProviderMarket());

        transactionEntity.addNonMandatoryFields(builder);

        return (Transaction) builder.build();
    }

    default CreditCardTransaction toCreditCardTransaction(
            TransactionEntity transactionEntity, CreditCardAccount account) {
        Builder builder =
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setAmount(transactionEntity.getSignedAmount())
                        .setDescription(transactionEntity.getTransactionInformation())
                        .setPending(transactionEntity.isPending())
                        .setMutable(transactionEntity.isMutable())
                        .setDate(transactionEntity.getDateOfTransaction())
                        .setTransactionDates(transactionEntity.getTransactionDates())
                        .setTransactionReference(transactionEntity.getTransactionReference())
                        .setProviderMarket(getProviderMarket());

        transactionEntity.addNonMandatoryFields(builder);
        return (CreditCardTransaction) builder.build();
    }

    default String getProviderMarket() {
        return "UK";
    }

    class DefaultTransactionMapper implements TransactionMapper {}
}
