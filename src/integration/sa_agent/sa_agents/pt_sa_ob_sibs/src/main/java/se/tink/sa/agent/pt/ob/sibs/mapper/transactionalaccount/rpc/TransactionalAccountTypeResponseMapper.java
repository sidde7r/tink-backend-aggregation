package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.stereotype.Component;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.TransactionalAccountType;

@Component
public class TransactionalAccountTypeResponseMapper
        implements Mapper<TransactionalAccountType, String> {

    @Override
    public TransactionalAccountType map(String source, MappingContext mappingContext) {
        TransactionalAccountType foundTransactionalAccountType = null;

        for (TransactionalAccountType accountType : TransactionalAccountType.values()) {
            if (accountType.name().equalsIgnoreCase(source)) {
                foundTransactionalAccountType = accountType;
                break;
            }
        }

        if (foundTransactionalAccountType == null) {
            return TransactionalAccountType.UNRECOGNIZED;
        }

        return foundTransactionalAccountType;
    }
}
