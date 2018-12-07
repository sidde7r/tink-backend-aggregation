package se.tink.backend.aggregation.agents.utils.mappers;

import com.google.common.annotations.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Account;

public class CoreAccountMapper {
    @VisibleForTesting
    static TypeMap<Account, se.tink.backend.aggregation.rpc.Account> toAggregationTypeMap = new ModelMapper()
            .typeMap(Account.class, se.tink.backend.aggregation.rpc.Account.class);
    @VisibleForTesting
    static TypeMap<se.tink.backend.aggregation.rpc.Account, Account> fromAggregationTypeMap = new ModelMapper()
            .typeMap(se.tink.backend.aggregation.rpc.Account.class, Account.class);

    public static se.tink.backend.aggregation.rpc.Account toAggregation(Account account) {
        return toAggregationTypeMap.map(account);
    }

    public static Account fromAggregation(se.tink.backend.aggregation.rpc.Account account) {
        return fromAggregationTypeMap.map(account);
    }
}
