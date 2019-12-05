package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.framework.mapper.ToDomainMapper;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

@Component
public class AmountEntityMapper implements ToDomainMapper<ExactCurrencyAmount, AmountEntity> {

    @Override
    public ExactCurrencyAmount mapToTransferModel(
            AmountEntity source, MappingContext mappingContext) {
        BigDecimal amount = new BigDecimal(source.getContent());
        ExactCurrencyAmount dest =
                ExactCurrencyAmount.newBuilder()
                        .setCurrencyCode(source.getCurrency())
                        .setUnscaledValue(amount.unscaledValue().longValue())
                        .setScale(amount.scale())
                        .build();
        return dest;
    }
}
