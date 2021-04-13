package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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
        return Transaction.builder()
                .setPending(isPending)
                .setDate(bookingDate)
                .setAmount(transactionAmount.toAmount())
                .setDescription(transactionText)
                .build();
    }
}
