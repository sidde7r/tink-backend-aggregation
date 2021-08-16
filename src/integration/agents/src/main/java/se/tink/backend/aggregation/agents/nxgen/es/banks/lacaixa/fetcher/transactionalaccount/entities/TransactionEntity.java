package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class TransactionEntity {

    @JsonUnwrapped private BalanceEntity amount;

    @JsonProperty("concepto")
    private String description;

    @JsonProperty("fechaValor")
    private DateEntity valueDate;

    @JsonProperty("fechaOperacion")
    private DateEntity bookingDate;

    @JsonProperty("referencia")
    private String reference;

    @JsonProperty("pendienteValidar")
    private boolean pending;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Transaction toTinkTransaction() {
        Transaction.Builder txBuilder =
                (Builder)
                        Transaction.builder()
                                .setAmount(amount.toExactCurrencyAmount())
                                .setDescription(description)
                                .setTransactionDates(getTransactionDates())
                                .setPending(pending)
                                .setMutable(pending)
                                .setProviderMarket(MarketCode.ES.toString())
                                .setRawDetails(this);

        if (Strings.isNullOrEmpty(reference)) {
            txBuilder.setTransactionReference(reference);
        }

        if (Objects.nonNull(valueDate)) {
            txBuilder.setDate(
                    Date.from(
                            valueDate
                                    .toTinkDate()
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()));
        }

        return txBuilder.build();
    }

    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (Objects.nonNull(valueDate)) {
            builder.setValueDate(new AvailableDateInformation(valueDate.toTinkDate()));
        }

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation(bookingDate.toTinkDate()));
        }

        return builder.build();
    }
}
