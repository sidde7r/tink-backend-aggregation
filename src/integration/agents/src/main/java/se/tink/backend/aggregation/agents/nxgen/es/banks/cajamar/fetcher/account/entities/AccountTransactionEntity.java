package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@Getter
@JsonObject
public class AccountTransactionEntity {
    private String documentId;
    private String description;
    private String date;
    private String currency;
    private BigDecimal amount;
    private BigDecimal balance;

    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDescription(description)
                        .setDate(LocalDate.parse(date))
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(
                                                new AvailableDateInformation(LocalDate.parse(date)))
                                        .setValueDate(
                                                new AvailableDateInformation(LocalDate.parse(date)))
                                        .build())
                        .setPending(false)
                        .setMutable(false)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                documentId)
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
