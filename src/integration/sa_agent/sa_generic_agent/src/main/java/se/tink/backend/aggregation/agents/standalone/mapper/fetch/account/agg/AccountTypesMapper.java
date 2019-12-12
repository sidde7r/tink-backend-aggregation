package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class AccountTypesMapper implements Mapper<TransactionalAccountType, se.tink.sa.services.fetch.account.TransactionalAccountType> {
    @Override
    public TransactionalAccountType map(se.tink.sa.services.fetch.account.TransactionalAccountType source, MappingContext context) {
        return TransactionalAccountType.valueOf(source.name());
    }
}
