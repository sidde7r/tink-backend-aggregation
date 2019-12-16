package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.mapper.common.BigDecimalMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

@Component
public class ExactCurrencyAmountMapper implements Mapper<ExactCurrencyAmount, AmountEntity> {

    @Autowired private BigDecimalMapper bigDecimalMapper;

    @Override
    public ExactCurrencyAmount map(AmountEntity source, MappingContext mappingContext) {
        ExactCurrencyAmount.Builder destBuilder = ExactCurrencyAmount.newBuilder();

        destBuilder.setCurrencyCode(source.getCurrency());
        destBuilder.setValue(bigDecimalMapper.map(source.getContent(), mappingContext));

        return destBuilder.build();
    }
}
