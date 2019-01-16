package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount;

import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class NordeaTransactionParser {

    public Optional<Transaction> toTinkTransaction(TransactionEntity transactionEntity) {
        if (noContent(transactionEntity.getAmount()) ||
                (noContent(transactionEntity.getTransactionDate()) && noContent(transactionEntity.getBookingDate()))) {
            return Optional.empty();

        }
        return Optional.of(Transaction.builder()
                .setDate(getDate(transactionEntity))
                .setDescription(getDescription(transactionEntity))
                .setAmount(new Amount(transactionEntity.getCurrency(), new BigDecimal(transactionEntity.getAmount())))
                .setExternalId(transactionEntity.getTransactionId())
                .setPending(NordeaBaseConstants.Transaction.RESERVED.equalsIgnoreCase(transactionEntity.getStatus()))
                .build());
    }

    protected String getDescription(TransactionEntity transactionEntity) {
        if (hasContent(transactionEntity.getOwnMessage())) {
            return transactionEntity.getOwnMessage();
        } else if (hasContent(transactionEntity.getMessage())) {
            return transactionEntity.getMessage();
        } else if (hasContent(transactionEntity.getTypeDescription())) {
            return transactionEntity.getTypeDescription();
        }

        return transactionEntity.getNarrative();
    }

    protected LocalDate getDate(TransactionEntity transactionEntity) {
        if (hasContent(transactionEntity.getTransactionDate())) {
            return LocalDate.parse(transactionEntity.getTransactionDate());
        } else if (hasContent(transactionEntity.getBookingDate())) {
            return LocalDate.parse(transactionEntity.getBookingDate());
        }

        return null;
    }

    protected boolean noContent(String s) {
        return Strings.nullToEmpty(s).trim().isEmpty();
    }

    protected boolean hasContent(String s) {
        return !noContent(s);
    }
}
