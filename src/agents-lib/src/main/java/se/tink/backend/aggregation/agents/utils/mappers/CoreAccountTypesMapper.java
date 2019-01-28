package se.tink.backend.aggregation.agents.utils.mappers;

import org.modelmapper.ModelMapper;
import se.tink.libraries.enums.AccountTypes;

public class CoreAccountTypesMapper {
    public static se.tink.backend.agents.rpc.AccountTypes toAggregation(AccountTypes accountType) {
        return new ModelMapper().map(accountType, se.tink.backend.agents.rpc.AccountTypes.class);
    }
}
