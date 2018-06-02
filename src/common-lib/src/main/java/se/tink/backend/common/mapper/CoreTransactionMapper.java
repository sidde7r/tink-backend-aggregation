package se.tink.backend.common.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Transaction;

/**
 * CoreTransactionMapper contains tools for converting {@link se.tink.backend.core.Transaction} objects to API objects consumed by other
 * services.
 * <p>
 * Historically (pre 2017-09) many services (system/main/aggregation) have used the same classes from the
 * {@link se.tink.backend.core} package. When making services more independent of each other it is increasingly
 * important to not share packages that have different purposes (client/server/database-models) for different services.
 * <p>
 * This will also have the added bonus that the Aggregation service will not depend on common-lib
 * or main-api when the migration is complete.
 */
public class CoreTransactionMapper {
    /**
     * ModelMapper for {@link se.tink.backend.core.Transaction} to System RPC Transaction
     */
    @VisibleForTesting
    public static final TypeMap<Transaction, se.tink.backend.system.rpc.Transaction> systemTransactionMap = new ModelMapper()
            .createTypeMap(Transaction.class, se.tink.backend.system.rpc.Transaction.class);

    /**
     * ModelMapper for System RPC Transcation to {@link se.tink.backend.core.Transaction}
     */
    @VisibleForTesting
    public static final TypeMap<se.tink.backend.system.rpc.Transaction, Transaction> coreTransactionMap = new ModelMapper()
            .createTypeMap(se.tink.backend.system.rpc.Transaction.class, Transaction.class);

    public static se.tink.backend.system.rpc.Transaction toSystemTransaction(Transaction transaction) {
        return systemTransactionMap.map(transaction);
    }

    public static List<se.tink.backend.system.rpc.Transaction> toSystemTransaction(List<Transaction> transactions) {
        return transactions.stream().map(systemTransactionMap::map).collect(Collectors.toList());
    }

    public static Transaction toCoreTransaction(se.tink.backend.system.rpc.Transaction transaction) {
        return coreTransactionMap.map(transaction);
    }

    public static List<Transaction> toCoreTransaction(List<se.tink.backend.system.rpc.Transaction> transactions) {
        return transactions.stream().map(coreTransactionMap::map).collect(Collectors.toList());
    }
}
