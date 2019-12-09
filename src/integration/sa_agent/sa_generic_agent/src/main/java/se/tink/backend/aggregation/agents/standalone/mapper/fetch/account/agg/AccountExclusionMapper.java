package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class AccountExclusionMapper
        implements Mapper<AccountExclusion, se.tink.sa.services.fetch.account.AccountExclusion> {

    @Override
    public AccountExclusion map(
            se.tink.sa.services.fetch.account.AccountExclusion source, MappingContext context) {
        return AccountExclusion.values()[source.getNumber()];
    }
}
