package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.STATUS_PENDING;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeDeserializer;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
public class NickelOperation {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime accountingDate;

    private BigDecimal amount;

    private String authorizationNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime bucket;

    private NickelOperationCategory category;

    private String counterpartBic;

    private String counterpartIban;

    private String counterpartName;

    private String currency;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime date;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime expectedAccountingDate;

    private NickelFee fee;

    private String id;

    private String label;

    private NickelAddress merchantAddress;

    private Long newBalance;
    private String pinMode;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime provisionDate;

    private String readMode;

    private String status;

    private String type;

    private String typeI18n;

    private String merchantName;

    private String mccCode;

    @JsonIgnore private static final ZoneId ZONE = ZoneId.of("CET");

    @JsonIgnore
    public String getDescription() {
        return Stream.of(
                        category != null ? category.getLabel() : "",
                        type,
                        label,
                        String.format("(%tT)", getDate()))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setTransactionDates(getTransactionDates())
                .setAmount(ExactCurrencyAmount.of(amount.movePointLeft(2).doubleValue(), currency))
                .setDescription(getDescription())
                .setDate(getDate().toLocalDate())
                .setPending(STATUS_PENDING.equals(status))
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, id)
                .build();
    }

    /*
        The method was added because the way the bank interpreted the time range
        led to the return of duplicate transactions
    */
    @JsonIgnore
    public boolean isInPage(Date fromDate, Date toDate) {
        return fromDate.toInstant().atZone(ZONE).toLocalDate().isBefore(getDate().toLocalDate())
                && toDate.toInstant().atZone(ZONE).toLocalDate().isAfter(getDate().toLocalDate());
    }

    @JsonIgnore
    private LocalDateTime getDate() {
        if (STATUS_PENDING.equals(status)) {
            return ObjectUtils.firstNonNull(date, expectedAccountingDate, provisionDate);
        }
        return ObjectUtils.firstNonNull(
                date, accountingDate, expectedAccountingDate, provisionDate);
    }

    @JsonIgnore
    private TransactionDates getTransactionDates() {
        Builder builder = TransactionDates.builder();
        Optional.ofNullable(accountingDate)
                .ifPresent(
                        xDate ->
                                builder.setBookingDate(
                                        new AvailableDateInformation(xDate.toLocalDate())));
        Optional.ofNullable(date)
                .ifPresent(
                        xDate ->
                                builder.setValueDate(
                                        new AvailableDateInformation(xDate.toLocalDate())));
        Optional.ofNullable(provisionDate)
                .ifPresent(
                        xDate ->
                                builder.setExecutionDate(
                                        new AvailableDateInformation(xDate.toLocalDate())));

        return builder.build();
    }
}
