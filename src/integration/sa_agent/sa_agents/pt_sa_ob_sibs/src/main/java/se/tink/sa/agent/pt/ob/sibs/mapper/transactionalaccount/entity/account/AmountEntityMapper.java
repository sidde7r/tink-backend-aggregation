package src.integration.sa_agent.sa_agents.pt_sa_ob_sibs.src.main.java.se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

@Component
public class AmountEntityMapper implements Mapper<ExactCurrencyAmount, AmountEntity> {

    @Override
    public ExactCurrencyAmount map(AmountEntity source, MappingContext mappingContext) {
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
