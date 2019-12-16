package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction.TransactionLinksEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

@Component
public class TransactionLinksEntityMapper
        implements Mapper<
                se.tink.sa.services.fetch.trans.TransactionLinksEntity, TransactionLinksEntity> {
    @Override
    public se.tink.sa.services.fetch.trans.TransactionLinksEntity map(
            TransactionLinksEntity source, MappingContext mappingContext) {
        se.tink.sa.services.fetch.trans.TransactionLinksEntity.Builder destBuilder =
                se.tink.sa.services.fetch.trans.TransactionLinksEntity.newBuilder();

        if (source.getFirst() != null) {
            destBuilder.setFirst(source.getFirst());
        }

        if (source.getLast() != null) {
            destBuilder.setLast(source.getLast());
        }

        if (source.getNext() != null) {
            destBuilder.setNext(source.getNext());
        }

        if (source.getViewAccount() != null) {
            destBuilder.setViewAccount(source.getViewAccount());
        }

        return destBuilder.build();
    }
}
