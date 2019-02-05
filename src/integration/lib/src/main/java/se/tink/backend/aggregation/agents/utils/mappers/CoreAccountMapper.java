package se.tink.backend.aggregation.agents.utils.mappers;

import com.google.common.annotations.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.libraries.account.rpc.Account;

public class CoreAccountMapper {
    @VisibleForTesting
    static TypeMap<Account, se.tink.backend.agents.rpc.Account> toAggregationTypeMap = new ModelMapper()
            .typeMap(Account.class, se.tink.backend.agents.rpc.Account.class);
    @VisibleForTesting
    static TypeMap<se.tink.backend.agents.rpc.Account, Account> fromAggregationTypeMap = new ModelMapper()
            .typeMap(se.tink.backend.agents.rpc.Account.class, Account.class);

    public static se.tink.backend.agents.rpc.Account toAggregation(Account account) {
        return toAggregationTypeMap.map(account);
    }

    public static Account fromAggregation(se.tink.backend.agents.rpc.Account account) {
        return fromAggregationTypeMap.map(account);
    }
}
