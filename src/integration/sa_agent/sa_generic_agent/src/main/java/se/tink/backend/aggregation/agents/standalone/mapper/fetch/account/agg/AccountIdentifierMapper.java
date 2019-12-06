package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.AccountIdentifierType;

public class AccountIdentifierMapper
        implements Mapper<AccountIdentifier, se.tink.sa.services.fetch.account.AccountIdentifier> {
    @Override
    public AccountIdentifier map(
            se.tink.sa.services.fetch.account.AccountIdentifier source, MappingContext context) {
        return AccountIdentifier.create(
                mapAccountIdentifierType(source.getType()), source.getId(), source.getName());
    }

    private AccountIdentifier.Type mapAccountIdentifierType(AccountIdentifierType type) {
        return AccountIdentifier.Type.values()[type.getNumber()];
    }
}
