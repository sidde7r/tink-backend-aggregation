package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class TransactionEntity {
    @JsonProperty("concept")
    private String description;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate operationDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate valueDate;

    private String amount;
    private String businessProcessNumber;

    private String parseAmount() {
        return amount.replace(",", ".");
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setDescription(description)
                        .setDate(operationDate)
                        .setAmount(ExactCurrencyAmount.of(parseAmount(), "EUR"))
                        .setTransactionDates(getTransactionDates())
                        .setTransactionReference(businessProcessNumber)
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }

    private TransactionDates getTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation(valueDate))
                .setBookingDate(new AvailableDateInformation(operationDate))
                .build();
    }
}
