package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private AmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountInfoEntity creditorAccount;
    private String debtorName;
    private TransactionAccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private TransactionDetailsLinksEntity links;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    public Transaction toTinkTransaction(boolean isPending) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toAmount())
                        .setDate(bookingDate)
                        .setDescription(remittanceInformationUnstructured)
                        .setPending(isPending)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates());

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(valueDate));

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }
}
