package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class CardTransactionDataEntity {
    private BigDecimal fee;
    private String establishment;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime date;

    private BigDecimal amount;
    private String currency;
    private String transaction;
    private String transactionType;
    private String sequence;
    private boolean outstandingTransaction;
    private boolean postpone;

    public Transaction toTinkTransaction(CreditCardAccount creditCardAccount) {
        LocalDate localDate = date.toLocalDate();
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(creditCardAccount)
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDescription(establishment)
                        .setDate(localDate)
                        .setPending(outstandingTransaction)
                        .setMutable(outstandingTransaction)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                sequence)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setValueDate(new AvailableDateInformation(localDate))
                                        .setBookingDate(new AvailableDateInformation(localDate))
                                        .build())
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
