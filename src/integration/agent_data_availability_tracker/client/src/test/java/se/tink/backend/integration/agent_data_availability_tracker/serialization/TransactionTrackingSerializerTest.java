package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDate;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.FieldEntry;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionTrackingSerializerTest {

    @Test
    public void ensureTransactionGetsSerializedProperly() {
        // given
        final DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.from(ZoneOffset.UTC));

        final Date date = new Date();
        final Instant instant = Instant.now();
        final List<TransactionDate> transactionDates = new ArrayList<>();
        final TransactionDate bookingDate = new TransactionDate();
        final AvailableDateInformation availableDateInformation = new AvailableDateInformation();

        availableDateInformation.setInstant(instant);
        bookingDate.setType(TransactionDateType.BOOKING_DATE);
        bookingDate.setValue(availableDateInformation);
        transactionDates.add(bookingDate);

        final Transaction transaction = new Transaction();
        transaction.setTransactionDates(transactionDates);
        transaction.setDate(date);

        final List<FieldEntry> entries =
                new TransactionTrackingSerializer(transaction, AccountTypes.CHECKING).buildList();

        Assert.assertEquals(
                "VALUE_NOT_LISTED",
                getFieldEntry(entries, "Transaction<CHECKING>.amount").getValue());
        Assert.assertEquals(
                "null", getFieldEntry(entries, "Transaction<CHECKING>.description").getValue());
        Assert.assertEquals(
                "VALUE_NOT_LISTED",
                getFieldEntry(entries, "Transaction<CHECKING>.originalAmount").getValue());
        Assert.assertEquals(
                "null", getFieldEntry(entries, "Transaction<CHECKING>.type").getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.proprietaryFinancialInstitutionType")
                        .getValue());
        Assert.assertEquals(
                "null", getFieldEntry(entries, "Transaction<CHECKING>.merchantName").getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.merchantCategoryCode").getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.transactionReference").getValue());
        Assert.assertEquals(
                dateFormatter.format(date.toInstant()),
                getFieldEntry(entries, "Transaction<CHECKING>.date").getValue());
        Assert.assertEquals(
                "null", getFieldEntry(entries, "Transaction<CHECKING>.mutability").getValue());
        Assert.assertEquals(
                "null", getFieldEntry(entries, "Transaction<CHECKING>.transactionId").getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.transactionAmount").getValue());
        Assert.assertEquals(
                dateFormatter.format(instant),
                getFieldEntry(entries, "Transaction<CHECKING>.transactionDate_BOOKING_DATE")
                        .getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.transactionDate_VALUE_DATE")
                        .getValue());
        Assert.assertEquals(
                "null",
                getFieldEntry(entries, "Transaction<CHECKING>.transactionDate_EXECUTION_DATE")
                        .getValue());
    }

    private FieldEntry getFieldEntry(List<FieldEntry> entries, String fieldName) {
        return entries.stream()
                .filter(entry -> fieldName.equals(entry.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find field " + fieldName));
    }
}
