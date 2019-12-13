package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.mapper.common.DateMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.BookedEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.TransactionEntity;

@Component
public class BookedEntityMapper implements Mapper<TransactionEntity, BookedEntity> {

    @Autowired private ExactCurrencyAmountMapper exactCurrencyAmountMapper;
    @Autowired private DateMapper dateMapper;

    @Override
    public TransactionEntity map(BookedEntity source, MappingContext mappingContext) {
        TransactionEntity.Builder destBuilder = TransactionEntity.newBuilder();

        destBuilder.setAmount(exactCurrencyAmountMapper.map(source.getAmount(), mappingContext));
        destBuilder.setValueDate(dateMapper.map(source.getValueDate(), mappingContext));
        destBuilder.setRemittanceInformationUnstructured(
                source.getRemittanceInformationUnstructured());

        return destBuilder.build();
    }
}
