package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.sa.common.mapper.Mapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.sa.common.mapper.MappingContext;

public class IbanIdModuleMapper implements Mapper<IdModule, se.tink.sa.services.fetch.account.IdModule> {

    //TODO: this should be generic and type base dispatched

    @Override
    public IdModule map(se.tink.sa.services.fetch.account.IdModule source, MappingContext mappingContext) {
        IdModule dest = IdModule.builder()
                .withUniqueIdentifier(source.getUniqueId())
                .withAccountNumber(source.getAccountNumber())
                .withAccountName(source.getAccountName())
                .addIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, source.getUniqueId()))
                .build();
        return dest;
    }
}
