package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.exceptions.StandaloneAgentIllegalStateException;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;
import src.integration.sa_agent.sa_agents.pt_sa_ob_sibs.src.main.java.se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account.AmountEntityMapper;

@Component
public class BalancesResponseMapper implements Mapper<ExactCurrencyAmount, BalancesResponse> {

    @Autowired private AmountEntityMapper amountEntityMapper;

    @Override
    public ExactCurrencyAmount mapToTransferModel(
            BalancesResponse source, MappingContext mappingContext) {

        AmountEntity amount =
                source.getBalances().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new StandaloneAgentIllegalStateException(
                                                SibsConstants.ErrorMessages.NO_BALANCE))
                        .getInterimAvailable()
                        .getAmount();
        ExactCurrencyAmount exactCurrencyAmount = amountEntityMapper.mapToTransferModel(amount);
        return exactCurrencyAmount;
    }
}
