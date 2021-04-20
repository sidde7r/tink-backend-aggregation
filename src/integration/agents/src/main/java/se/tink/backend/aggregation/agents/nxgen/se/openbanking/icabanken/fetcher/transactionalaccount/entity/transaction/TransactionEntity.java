package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String entryReference;
    private TransactionAmountEntity transactionAmount;
    private List<ExchangeRateEntity> exchangeRate;
    private String creditorName;
    private CreditorAccountEntity creditorAccount;
    private String debtorName;
    private String remittanceInformationUnstructured;
    private String transactionText;

    public Transaction toTinkTransaction(boolean isPending) {
        Builder builder =
                Transaction.builder()
                        .setPending(isPending)
                        .setDate(bookingDate)
                        .setAmount(transactionAmount.toAmount())
                        .setDescription(transactionText)
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                entryReference)
                        .setProviderMarket(IcaBankenConstants.PROVIDER_MARKET);

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(valueDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
