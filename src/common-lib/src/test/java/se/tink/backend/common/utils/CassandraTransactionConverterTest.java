package se.tink.backend.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPart;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionConverterTest {

    private static void assertEquals(Transaction expected, Transaction actual) {
        Assert.assertEquals(expected.getAmount(), actual.getAmount(), 0.001);
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getCategoryId(), actual.getCategoryId());
        Assert.assertEquals(expected.getCategoryType(), actual.getCategoryType());
        Assert.assertEquals(expected.getCredentialsId(), actual.getCredentialsId());
        Assert.assertEquals(expected.getDate(), actual.getDate());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getFormattedDescription(), actual.getFormattedDescription());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getInserted(), actual.getInserted());
        Assert.assertEquals(expected.getLastModified(), actual.getLastModified());

        // Locations aren't migrated to Cassandra.
        //assertEquals(expected.getLocation(), actual.getLocation());

        Assert.assertEquals(expected.getMerchantId(), actual.getMerchantId());
        Assert.assertEquals(expected.getNotes(), actual.getNotes());
        Assert.assertEquals(expected.getOriginalDescription(), actual.getOriginalDescription());
        Assert.assertEquals(expected.getOriginalAmount(), actual.getOriginalAmount(), 0.000001);
        Assert.assertEquals(expected.getOriginalDate(), actual.getOriginalDate());

        Assert.assertEquals(expected.getPayload(), actual.getPayload());

        Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertEquals(expected.isUserModifiedAmount(), actual.isUserModifiedAmount());
        Assert.assertEquals(expected.isUserModifiedCategory(), actual.isUserModifiedCategory());
        Assert.assertEquals(expected.isUserModifiedDate(), actual.isUserModifiedDate());
        Assert.assertEquals(expected.isUserModifiedDescription(), actual.isUserModifiedDescription());
        Assert.assertEquals(expected.isUserModifiedLocation(), actual.isUserModifiedLocation());

        Assert.assertEquals(expected.getDispensableAmount(), actual.getDispensableAmount());
        Assert.assertEquals(expected.hasParts(), actual.hasParts());

        if (expected.hasParts()) {
            Assert.assertEquals(expected.getParts().size(), actual.getParts().size());

            for (int i = 0; i < expected.getParts().size(); i++) {
                assertEquals(expected.getParts().get(i), actual.getParts().get(i));
            }
        }
    }

    private static void assertEquals(TransactionPart expected, TransactionPart actual) {
        Assert.assertEquals(expected.getAmount(), actual.getAmount());
        Assert.assertEquals(expected.getCategoryId(), actual.getCategoryId());
        Assert.assertEquals(expected.getCounterpartId(), actual.getCounterpartId());
        Assert.assertEquals(expected.getCounterpartTransactionId(), actual.getCounterpartTransactionId());
        Assert.assertEquals(expected.getDate(), actual.getDate());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getLastModified(), actual.getLastModified());
    }

    private Random random = new Random();

    private Map<TransactionPayloadTypes, String> buildRandomPayload() {
        Map<TransactionPayloadTypes, String> result = Maps.newHashMap();
        TransactionPayloadTypes[] transactionPayloadTypes = TransactionPayloadTypes.values();
        RandomSample<TransactionPayloadTypes> sampler = RandomSample.from(Lists.newArrayList(transactionPayloadTypes));
        for (TransactionPayloadTypes type : sampler.pick(random.nextInt(transactionPayloadTypes.length))) {
            result.put(type, StringUtils.generateUUID());
        }

        return result;
    }

    private Transaction buildRandomTransaction() {
        // Using UUIDs for placeholder random text in various parts of this method.

        final Transaction t = new Transaction();

        t.setUserId(StringUtils.generateUUID());

        t.setAccountId(StringUtils.generateUUID());
        t.setAmount((random.nextDouble() * 2000) - 1000);
        t.setCategory(buildRandomCategory());

        t.setCredentialsId(StringUtils.generateUUID());
        t.setDate(randomDate());
        t.setDescription(StringUtils.generateUUID());
        t.setFormattedDescription(StringUtils.generateUUID());
        t.setInserted(random.nextLong());
        t.setLastModified(randomDate());

        t.setMerchantId(StringUtils.generateUUID());
        t.setNotes(StringUtils.generateUUID());
        t.setOriginalAmount((random.nextDouble() * 2000) - 1000);
        t.setOriginalDate(randomDate());
        t.setOriginalDescription(StringUtils.generateUUID());

        // There are also other ways of setting the payload.
        t.setPayload(buildRandomPayload());

        t.setTimestamp(random.nextLong());

        final TransactionTypes[] transactionTypes = TransactionTypes.values();
        t.setType(transactionTypes[random.nextInt(transactionTypes.length)]);

        t.setUserModifiedAmount(random.nextBoolean());
        t.setUserModifiedCategory(random.nextBoolean());
        t.setUserModifiedDate(random.nextBoolean());
        t.setUserModifiedDescription(random.nextBoolean());
        t.setUserModifiedLocation(random.nextBoolean());

        if (random.nextBoolean()) {
            // Create parts.

            List<TransactionPart> parts = Lists.newArrayList();

            double sign = Math.signum(t.getAmount());
            double amountLeft = Math.abs(t.getAmount());
            double partResolution = amountLeft / 5d;

            while (amountLeft > 0) {
                int x = random.nextInt(4) + 1;
                double partAmount = Math.min(x * partResolution, amountLeft);
                amountLeft -= partAmount;

                TransactionPart tp = new TransactionPart();

                tp.setAmount(BigDecimal.valueOf(sign * partAmount));
                tp.setCategoryId(StringUtils.generateUUID());
                tp.setCounterpartId(StringUtils.generateUUID());
                tp.setCounterpartTransactionId(StringUtils.generateUUID());
                tp.setId(StringUtils.generateUUID());
                tp.setLastModified(randomDate());

                parts.add(tp);
            }

            t.setParts(parts);
        }

        return t;
    }

    private Category buildRandomCategory() {
        final CategoryTypes[] categoryTypes = CategoryTypes.values();

        Category category = new Category();
        category.setId(StringUtils.generateUUID());
        category.setType(categoryTypes[random.nextInt(categoryTypes.length)]);

        return category;
    }

    private Date randomDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        int seconds_per_year = 265 * 24 * 60 * 60;
        calendar.add(Calendar.SECOND, random.nextInt(seconds_per_year));
        return calendar.getTime();
    }

    @Test
    public void testTransactionConversion() {
        Transaction original = buildRandomTransaction();
        Transaction calculated = CassandraTransactionConverter.fromCassandraTransaction(CassandraTransactionConverter
                .toCassandraTransaction(original));

        System.out.println(String.format("Original: %s", original));
        System.out.println(String.format("Calculated: %s", calculated));

        assertEquals(original, calculated);
    }

    @Test
    public void testXor() {
        Date aDate = null;
        Date bDate = new Date();
        Assert.assertTrue(aDate == null ^ bDate == null);

        aDate = new Date();
        bDate = null;
        Assert.assertTrue(aDate == null ^ bDate == null);

        aDate = null;
        bDate = null;
        Assert.assertFalse(aDate == null ^ bDate == null);

        aDate = new Date();
        bDate = new Date();
        Assert.assertFalse(aDate == null ^ bDate == null);
    }

    @Test
    public void testNullConversions() {
        Assertions.assertThat(CassandraTransactionConverter.fromCassandraTransaction(null)).isNull();
        Assertions.assertThat(CassandraTransactionConverter.toCassandraTransaction(null)).isNull();
        Assertions.assertThat(CassandraTransactionConverter.toCassandraTransactionDeleted(null)).isNull();
    }

    @Test
    public void testPeriodConversion() {
        Transaction tx = buildRandomTransaction();
        int period = tx.transformDateToPeriod();
        UUID id = UUIDUtils.fromTinkUUID(tx.getUserId());

        CassandraPeriodByUserId p = CassandraTransactionConverter.toCassandraPeriodByUserId(tx);

        Assert.assertEquals(period, p.getPeriod());
        Assert.assertEquals(id, p.getUserId());
    }
}
