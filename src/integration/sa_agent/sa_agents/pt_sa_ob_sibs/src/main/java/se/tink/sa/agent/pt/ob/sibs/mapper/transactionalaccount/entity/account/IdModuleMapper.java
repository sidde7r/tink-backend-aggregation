package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.mapper.common.IbanAccountIdentifierMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.IdModule;

@Component
public class IdModuleMapper implements Mapper<IdModule, AccountEntity> {

    @Autowired IbanAccountIdentifierMapper ibanAccountIdentifierMapper;

    @Override
    public IdModule map(AccountEntity source, MappingContext mappingContext) {
        IdModule.Builder destBuilder = IdModule.newBuilder();
        destBuilder.setUniqueId(source.getIban());
        destBuilder.setAccountNumber(source.getIban());
        destBuilder.setAccountName(source.getName());
        destBuilder.addIdentifiers(ibanAccountIdentifierMapper.map(source, mappingContext));

        return destBuilder.build();
    }
}
