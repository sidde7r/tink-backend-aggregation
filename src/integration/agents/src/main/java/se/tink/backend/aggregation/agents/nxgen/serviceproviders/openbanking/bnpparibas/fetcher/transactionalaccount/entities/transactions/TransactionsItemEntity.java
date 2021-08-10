package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.transactions;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionsItemEntity {

    private String resourceId;

    private List<String> remittanceInformation;

    private AmountEntity transactionAmount;

    private Date bookingDate;

    private String creditDebitIndicator;

    private String entryReference;

    private LocalDate valueDate;

    private String status;

    private boolean getPending() {
        return status.equalsIgnoreCase(BnpParibasBaseConstants.ResponseValues.PENDING_TRANSACTION);
    }

    public Transaction toTinkTransactions() {
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation().setDate(valueDate))
                        .build();

        return Transaction.builder()
                .setTransactionDates(transactionDates)
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, resourceId)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setTransactionDates(transactionDates)
                .setTransactionReference(entryReference)
                .setDescription(
                        remittanceInformation.stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator))
                .setPending(getPending())
                .build();
    }
}
