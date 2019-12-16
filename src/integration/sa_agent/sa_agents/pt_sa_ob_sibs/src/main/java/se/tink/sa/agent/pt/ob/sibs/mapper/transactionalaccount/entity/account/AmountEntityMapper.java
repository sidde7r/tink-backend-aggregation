package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.mapper.common.BigDecimalMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

@Component
public class AmountEntityMapper implements Mapper<ExactCurrencyAmount, AmountEntity> {

    @Autowired private BigDecimalMapper bigDecimalMapper;

    @Override
    public ExactCurrencyAmount map(AmountEntity source, MappingContext mappingContext) {
        ExactCurrencyAmount dest =
                ExactCurrencyAmount.newBuilder()
                        .setCurrencyCode(source.getCurrency())
                        .setValue(bigDecimalMapper.map(source.getContent(), mappingContext))
                        .build();
        return dest;
    }
}
