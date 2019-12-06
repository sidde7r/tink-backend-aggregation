package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.TransactionalAccountType;

public class AccountTypesMapper implements Mapper<AccountTypes, TransactionalAccountType> {
    @Override
    public AccountTypes map(TransactionalAccountType source, MappingContext context) {
        return AccountTypes.values()[source.getNumber()];
    }
}
