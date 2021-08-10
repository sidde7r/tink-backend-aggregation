package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class ConsorsbankTransactionMapper implements TransactionMapper {
    public Optional<AggregationTransaction> toTinkTransaction(
            TransactionEntity transactionEntity, boolean isPending) {
        Transaction transaction = null;
        try {
            transaction =
                    Transaction.builder()
                            .setPending(isPending)
                            .setAmount(transactionEntity.getTransactionAmount().toTinkAmount())
                            .setDate(
                                    ObjectUtils.firstNonNull(
                                            transactionEntity.getBookingDate(),
                                            transactionEntity.getValueDate()))
                            .setDescription(buildDescription(transactionEntity))
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse transaction, it will be skipped.", e);
        }
        return Optional.ofNullable(transaction);
    }

    // NZG-751
    // It was decided that the unstructured description the bank provides is really poor quality.
    // So we are putting few fields together to build our own description, with some null checks on
    // fields that are not always present.
    // We currently do not worry about translations.
    private String buildDescription(TransactionEntity transactionEntity) {
        StringBuilder builder = new StringBuilder("Transfer from");
        addName(transactionEntity.getDebtorName(), builder);
        addAccountNumber(transactionEntity.getDebtorAccount().getIban(), builder);
        builder.append(" Transfer to");
        addName(transactionEntity.getCreditorName(), builder);
        addAccountNumber(transactionEntity.getCreditorAccount().getIban(), builder);

        builder.append(". Additional transaction description: ");
        builder.append(transactionEntity.getRemittanceInformationUnstructured());

        return builder.toString();
    }

    private void addName(String name, StringBuilder builder) {
        if (name != null) {
            builder.append(": ");
            builder.append(name);
            builder.append(",");
        }
    }

    private void addAccountNumber(String accountNumber, StringBuilder builder) {
        builder.append(" account number: ");
        builder.append(accountNumber);
    }
}
