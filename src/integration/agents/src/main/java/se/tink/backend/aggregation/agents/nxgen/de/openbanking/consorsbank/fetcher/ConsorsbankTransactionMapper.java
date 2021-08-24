package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class ConsorsbankTransactionMapper implements TransactionMapper {

    private static final Set<String> SPECIAL_CREDITORS = Sets.newHashSet("PayPal", "Klarna");

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

    // NZG-790
    // It was decided that the unstructured description the bank provides is really poor quality.
    // So we are putting few fields together to build our own description, with some null checks on
    // fields that are not always present.
    // We currently do not worry about translations.
    private String buildDescription(TransactionEntity transactionEntity) {
        if (isIncomeTransaction(transactionEntity)) {
            return getIncomeDescription(transactionEntity);
        }

        return getPurchaseDescription(transactionEntity).trim();
    }

    private String getIncomeDescription(TransactionEntity transactionEntity) {
        if (!isFieldEmpty(transactionEntity.getDebtorName())) {
            return transactionEntity.getDebtorName();
        }

        if (!isFieldEmpty(transactionEntity.getRemittanceInformationUnstructured())) {
            return transactionEntity.getRemittanceInformationUnstructured();
        }

        return "";
    }

    /**
     * Prioritize: 1. CreditorName if special creditor and RemittanceInformationUnstructured exists
     * 2.CreditorName if special creditor and RemittanceInformationUnstructured not exists
     * 3.CreditorName if CreditorName exists 4. RemittanceInformationUnstructured if
     * RemittanceInformationUnstructured exists and CreditorName not exists
     */
    private String getPurchaseDescription(TransactionEntity transactionEntity) {

        if (isSpecialCreditor(transactionEntity)
                && !isFieldEmpty(transactionEntity.getRemittanceInformationUnstructured())) {
            return transactionEntity.getRemittanceInformationUnstructured().trim();
        }

        if (isSpecialCreditor(transactionEntity)
                && !isFieldEmpty(transactionEntity.getCreditorName())) {
            return transactionEntity.getCreditorName().trim();
        }

        if (!isFieldEmpty(transactionEntity.getCreditorName())) {
            return transactionEntity.getCreditorName().trim();
        }

        if (!isFieldEmpty(transactionEntity.getRemittanceInformationUnstructured())) {
            return transactionEntity.getRemittanceInformationUnstructured().trim();
        }

        return "";
    }

    private boolean isIncomeTransaction(TransactionEntity transactionEntity) {
        BigDecimal transactionAmount =
                Optional.ofNullable(transactionEntity.getTransactionAmount().getAmount())
                        .orElse(BigDecimal.ZERO);
        return transactionAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isFieldEmpty(String fieldValue) {
        return Optional.ofNullable(fieldValue).map(field -> field.trim().isEmpty()).orElse(true);
    }

    private boolean isSpecialCreditor(TransactionEntity transactionEntity) {
        return SPECIAL_CREDITORS.contains(transactionEntity.getCreditorName());
    }
}
