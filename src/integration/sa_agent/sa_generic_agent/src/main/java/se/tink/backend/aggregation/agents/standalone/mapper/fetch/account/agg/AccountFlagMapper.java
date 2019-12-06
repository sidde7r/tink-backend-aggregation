package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.libraries.account.enums.AccountFlag;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class AccountFlagMapper
        implements Mapper<AccountFlag, se.tink.sa.services.fetch.account.AccountFlag> {
    @Override
    public AccountFlag map(
            se.tink.sa.services.fetch.account.AccountFlag source, MappingContext mappingContext) {
        return AccountFlag.values()[source.getNumber()];
    }
}
