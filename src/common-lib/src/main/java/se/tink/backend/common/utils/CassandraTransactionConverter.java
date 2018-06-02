package se.tink.backend.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.CassandraTransactionByUserIdPeriod;
import se.tink.backend.core.CassandraTransactionDeleted;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPart;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionConverter {

    private static final TypeReference<List<TransactionPart>> LIST_OF_TRANSACTION_PARTS = new TypeReference<List<TransactionPart>>() {
    };

    public static final Function<CassandraTransaction, Transaction> FROM_CASSANDRA = CassandraTransactionConverter::fromCassandraTransaction;

    public static final Function<Transaction, CassandraTransaction> TO_CASSANDRA = CassandraTransactionConverter::toCassandraTransaction;

    public static final Function<CassandraTransactionByUserIdPeriod, Transaction> FROM_CASSANDRA_BY_PERIOD = CassandraTransactionConverter::fromCassandraTransactionByUserIdAndPeriod;

    public static final Function<Transaction, CassandraTransactionByUserIdPeriod> TO_CASSANDRA_BY_PERIOD = CassandraTransactionConverter::toCassandraTransactionByUserIdAndPeriod;

    public static final Function<Transaction, CassandraPeriodByUserId> TO_CASSANDRA_PERIOD_BY_USERID = CassandraTransactionConverter::toCassandraPeriodByUserId;

    public static Transaction fromCassandraTransactionDeleted(CassandraTransactionDeleted ctd) {
        if (ctd == null) {
            return null;
        }

        final Transaction t = new Transaction();

        t.setAccountId(toNullableTinkUUID(ctd.getAccountId()));
        if (ctd.getExactAmount() != null) {
            t.setAmount(ctd.getExactAmount().doubleValue());
        } else if (ctd.getAmount() != null) {
            t.setAmount(ctd.getAmount());
        } else if (ctd.getOriginalAmount() != null) {
            t.setAmount(ctd.getOriginalAmount());
        } else {
            t.setAmount(null);
        }
        t.setCategoryId(toNullableTinkUUID(ctd.getCategoryId()));
        t.setCategoryType(ctd.getCategoryType());
        t.setCredentialsId(toNullableTinkUUID(ctd.getCredentialsId()));
        t.setDate(ctd.getDate());
        t.setDescription(ctd.getDescription());
        t.setFormattedDescription(ctd.getFormattedDescription());
        t.setId(toNullableTinkUUID(ctd.getId()));
        t.setInserted(ctd.getInserted());
        t.setInternalPayloadSerialized(ctd.getInternalPayloadSerialized());
        t.setLastModified(ctd.getLastModified());
        t.setMerchantId(toNullableTinkUUID(ctd.getMerchantId()));
        t.setNotes(ctd.getNotes());
        t.setOriginalAmount(ctd.getExactOriginalAmount().doubleValue());
        t.setOriginalDate(ctd.getOriginalDate());
        t.setOriginalDescription(ctd.getOriginalDescription());
        t.setPayloadSerialized(ctd.getPayloadSerialized());
        t.setPending(ctd.isPending());
        t.setTimestamp(ctd.getTimestamp());
        t.setType(ctd.getType());
        t.setUserId(toNullableTinkUUID(ctd.getUserId()));
        t.setUserModifiedAmount(ctd.isUserModifiedAmount());
        t.setUserModifiedCategory(ctd.isUserModifiedCategory());
        t.setUserModifiedDate(ctd.isUserModifiedDate());
        t.setUserModifiedDescription(ctd.isUserModifiedDescription());
        t.setUserModifiedLocation(ctd.isUserModifiedLocation());

        if (ctd.getPayloadSerialized() != null) {
            t.setParts(SerializationUtils.deserializeFromString(ctd.getPartsSerialized(), LIST_OF_TRANSACTION_PARTS));
        }

        return t;
    }

    public static Transaction fromCassandraTransaction(CassandraTransaction ct) {
        if (ct == null) {
            return null;
        }

        final Transaction t = new Transaction();

        t.setAccountId(toNullableTinkUUID(ct.getAccountId()));
        t.setAmount(Optional.ofNullable(ct.getAmount()).map(BigDecimal::doubleValue).orElse(null));
        t.setCategoryId(toNullableTinkUUID(ct.getCategoryId()));
        t.setCategoryType(ct.getCategoryType());
        t.setCredentialsId(toNullableTinkUUID(ct.getCredentialsId()));
        t.setDate(ct.getDate());
        t.setDescription(ct.getDescription());
        t.setFormattedDescription(ct.getFormattedDescription());
        t.setId(toNullableTinkUUID(ct.getId()));
        t.setInserted(ct.getInserted());
        t.setInternalPayloadSerialized(ct.getInternalPayloadSerialized());
        t.setLastModified(ct.getLastModified());
        t.setMerchantId(toNullableTinkUUID(ct.getMerchantId()));
        t.setNotes(ct.getNotes());
        t.setOriginalAmount(ct.getOriginalAmount().doubleValue());
        t.setOriginalDate(ct.getOriginalDate());
        t.setOriginalDescription(ct.getOriginalDescription());
        t.setPayloadSerialized(ct.getPayloadSerialized());
        t.setPending(ct.isPending());
        t.setTimestamp(ct.getTimestamp());
        t.setType(ct.getType());
        t.setUserId(toNullableTinkUUID(ct.getUserId()));
        t.setUserModifiedAmount(ct.isUserModifiedAmount());
        t.setUserModifiedCategory(ct.isUserModifiedCategory());
        t.setUserModifiedDate(ct.isUserModifiedDate());
        t.setUserModifiedDescription(ct.isUserModifiedDescription());
        t.setUserModifiedLocation(ct.isUserModifiedLocation());

        if (ct.getPayloadSerialized() != null) {
            t.setParts(SerializationUtils.deserializeFromString(ct.getPartsSerialized(), LIST_OF_TRANSACTION_PARTS));
        }

        return t;
    }

    private static UUID fromNullableTinkUUID(String uuid) {
        return Optional.ofNullable(uuid).map(UUIDUtils.FROM_TINK_UUID_TRANSFORMER::apply).orElse(null);
    }

    public static Transaction fromCassandraTransactionByUserIdAndPeriod(CassandraTransactionByUserIdPeriod ct) {
        if (ct == null) {
            return null;
        }

        final Transaction t = new Transaction();

        t.setAccountId(toNullableTinkUUID(ct.getAccountId()));
        t.setAmount(ct.getExactAmount().doubleValue());
        t.setCategoryId(toNullableTinkUUID(ct.getCategoryId()));
        t.setCategoryType(ct.getCategoryType());
        t.setCredentialsId(toNullableTinkUUID(ct.getCredentialsId()));
        t.setDate(ct.getDate());
        t.setDescription(ct.getDescription());
        t.setFormattedDescription(ct.getFormattedDescription());
        t.setId(toNullableTinkUUID(ct.getId()));
        t.setInserted(ct.getInserted());
        t.setInternalPayloadSerialized(ct.getInternalPayloadSerialized());
        t.setLastModified(ct.getLastModified());
        t.setMerchantId(toNullableTinkUUID(ct.getMerchantId()));
        t.setNotes(ct.getNotes());
        t.setOriginalAmount(ct.getExactOriginalAmount().doubleValue());
        t.setOriginalDate(ct.getOriginalDate());
        t.setOriginalDescription(ct.getOriginalDescription());
        t.setPayloadSerialized(ct.getPayloadSerialized());
        t.setPending(ct.isPending());
        t.setTimestamp(ct.getTimestamp());
        t.setType(ct.getType());
        t.setUserId(toNullableTinkUUID(ct.getUserId()));
        t.setUserModifiedAmount(ct.isUserModifiedAmount());
        t.setUserModifiedCategory(ct.isUserModifiedCategory());
        t.setUserModifiedDate(ct.isUserModifiedDate());
        t.setUserModifiedDescription(ct.isUserModifiedDescription());
        t.setUserModifiedLocation(ct.isUserModifiedLocation());

        if (ct.getPayloadSerialized() != null) {
            t.setParts(SerializationUtils.deserializeFromString(ct.getPartsSerialized(), LIST_OF_TRANSACTION_PARTS));
        }

        return t;
    }

    public static CassandraTransactionByUserIdPeriod toCassandraTransactionByUserIdAndPeriod(Transaction t) {
        if (t == null) {
            return null;
        }

        final CassandraTransactionByUserIdPeriod cassandraTransactionByUserIdPeriod = new CassandraTransactionByUserIdPeriod();

        cassandraTransactionByUserIdPeriod.setPeriod(t.transformDateToPeriod());
        cassandraTransactionByUserIdPeriod.setAccountId(fromNullableTinkUUID(t.getAccountId()));
        cassandraTransactionByUserIdPeriod.setExactAmount(BigDecimal.valueOf(t.getAmount()));
        cassandraTransactionByUserIdPeriod.setCategoryId(fromNullableTinkUUID(t.getCategoryId()));
        cassandraTransactionByUserIdPeriod.setCredentialsId(fromNullableTinkUUID(t.getCredentialsId()));
        cassandraTransactionByUserIdPeriod.setDate(t.getDate());
        cassandraTransactionByUserIdPeriod.setDescription(t.getDescription());
        cassandraTransactionByUserIdPeriod.setFormattedDescription(t.getFormattedDescription());
        cassandraTransactionByUserIdPeriod.setId(fromNullableTinkUUID(t.getId()));
        cassandraTransactionByUserIdPeriod.setInserted(t.getInserted());
        cassandraTransactionByUserIdPeriod.setInternalPayloadSerialized(t.getInternalPayloadSerialized());
        cassandraTransactionByUserIdPeriod.setLastModified(t.getLastModified());
        cassandraTransactionByUserIdPeriod.setNotes(t.getNotes());
        cassandraTransactionByUserIdPeriod.setExactOriginalAmount(BigDecimal.valueOf(t.getOriginalAmount()));
        cassandraTransactionByUserIdPeriod.setOriginalDate(t.getOriginalDate());
        cassandraTransactionByUserIdPeriod.setOriginalDescription(t.getOriginalDescription());
        cassandraTransactionByUserIdPeriod.setPayloadSerialized(t.getPayloadSerialized());
        cassandraTransactionByUserIdPeriod.setPending(t.isPending());
        cassandraTransactionByUserIdPeriod.setTimestamp(t.getTimestamp());
        cassandraTransactionByUserIdPeriod.setUserId(fromNullableTinkUUID(t.getUserId()));
        cassandraTransactionByUserIdPeriod.setType(t.getType());
        cassandraTransactionByUserIdPeriod.setCategoryType(t.getCategoryType());
        cassandraTransactionByUserIdPeriod.setMerchantId(fromNullableTinkUUID(t.getMerchantId()));
        cassandraTransactionByUserIdPeriod.setUserModifiedAmount(t.isUserModifiedAmount());
        cassandraTransactionByUserIdPeriod.setUserModifiedCategory(t.isUserModifiedCategory());
        cassandraTransactionByUserIdPeriod.setUserModifiedDate(t.isUserModifiedDate());
        cassandraTransactionByUserIdPeriod.setUserModifiedDescription(t.isUserModifiedDescription());
        cassandraTransactionByUserIdPeriod.setUserModifiedLocation(t.isUserModifiedLocation());

        if (t.hasParts()) {
            cassandraTransactionByUserIdPeriod.setPartsSerialized(SerializationUtils.serializeToString(t.getParts()));
        }

        return cassandraTransactionByUserIdPeriod;
    }

    public static CassandraTransaction toCassandraTransaction(Transaction t) {
        if (t == null) {
            return null;
        }

        final CassandraTransaction ct = new CassandraTransaction();

        ct.setAccountId(fromNullableTinkUUID(t.getAccountId()));
        ct.setAmount(BigDecimal.valueOf(t.getAmount()));
        ct.setCategoryId(fromNullableTinkUUID(t.getCategoryId()));
        ct.setCredentialsId(fromNullableTinkUUID(t.getCredentialsId()));
        ct.setDate(t.getDate());
        ct.setDescription(t.getDescription());
        ct.setFormattedDescription(t.getFormattedDescription());
        ct.setId(fromNullableTinkUUID(t.getId()));
        ct.setInserted(t.getInserted());
        ct.setInternalPayloadSerialized(t.getInternalPayloadSerialized());
        ct.setLastModified(t.getLastModified());
        ct.setNotes(t.getNotes());
        ct.setOriginalAmount(BigDecimal.valueOf(t.getOriginalAmount()));
        ct.setOriginalDate(t.getOriginalDate());
        ct.setOriginalDescription(t.getOriginalDescription());
        ct.setPayloadSerialized(t.getPayloadSerialized());
        ct.setPending(t.isPending());
        ct.setTimestamp(t.getTimestamp());
        ct.setUserId(fromNullableTinkUUID(t.getUserId()));
        ct.setType(t.getType());
        ct.setCategoryType(t.getCategoryType());
        ct.setMerchantId(fromNullableTinkUUID(t.getMerchantId()));
        ct.setUserModifiedAmount(t.isUserModifiedAmount());
        ct.setUserModifiedCategory(t.isUserModifiedCategory());
        ct.setUserModifiedDate(t.isUserModifiedDate());
        ct.setUserModifiedDescription(t.isUserModifiedDescription());
        ct.setUserModifiedLocation(t.isUserModifiedLocation());

        if (t.hasParts()) {
            ct.setPartsSerialized(SerializationUtils.serializeToString(t.getParts()));
        }

        return ct;
    }

    public static CassandraTransactionDeleted toCassandraTransactionDeleted(Transaction t) {
        if (t == null) {
            return null;
        }

        final CassandraTransactionDeleted ct = new CassandraTransactionDeleted();

        ct.setAccountId(fromNullableTinkUUID(t.getAccountId()));
        ct.setAmount(BigDecimal.valueOf(t.getAmount()));
        ct.setCategoryId(fromNullableTinkUUID(t.getCategoryId()));
        ct.setCategoryType(t.getCategoryType());
        ct.setCredentialsId(fromNullableTinkUUID(t.getCredentialsId()));
        ct.setDate(t.getDate());
        ct.setDescription(t.getDescription());
        ct.setFormattedDescription(t.getFormattedDescription());
        ct.setId(fromNullableTinkUUID(t.getId()));
        ct.setInserted(t.getInserted());
        ct.setInternalPayloadSerialized(t.getInternalPayloadSerialized());
        ct.setLastModified(t.getLastModified());
        ct.setNotes(t.getNotes());
        ct.setExactOriginalAmount(BigDecimal.valueOf(t.getOriginalAmount()));
        ct.setOriginalDate(t.getOriginalDate());
        ct.setOriginalDescription(t.getOriginalDescription());
        ct.setPayloadSerialized(t.getPayloadSerialized());
        ct.setPending(t.isPending());
        ct.setTimestamp(t.getTimestamp());
        ct.setType(t.getType());
        ct.setUserId(fromNullableTinkUUID(t.getUserId()));
        ct.setMerchantId(fromNullableTinkUUID(t.getMerchantId()));
        ct.setUserModifiedAmount(t.isUserModifiedAmount());
        ct.setUserModifiedCategory(t.isUserModifiedCategory());
        ct.setUserModifiedDate(t.isUserModifiedDate());
        ct.setUserModifiedDescription(t.isUserModifiedDescription());
        ct.setUserModifiedLocation(t.isUserModifiedLocation());

        if (t.hasParts()) {
            ct.setPartsSerialized(SerializationUtils.serializeToString(t.getParts()));
        }

        return ct;
    }

    public static CassandraPeriodByUserId toCassandraPeriodByUserId(Transaction t) {
        if (t == null) {
            return null;
        }

        final CassandraPeriodByUserId cp = new CassandraPeriodByUserId();
        cp.setUserId(fromNullableTinkUUID(t.getUserId()));
        cp.setPeriod(t.transformDateToPeriod());
        return cp;
    }

    private static String toNullableTinkUUID(UUID uuid) {
        return Optional.ofNullable(uuid).map(UUIDUtils.TO_TINK_UUID_TRANSFORMER::apply).orElse(null);
    }
}
