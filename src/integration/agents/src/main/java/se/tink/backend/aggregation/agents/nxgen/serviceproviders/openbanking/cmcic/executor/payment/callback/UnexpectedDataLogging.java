package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

@Slf4j
public class UnexpectedDataLogging implements CmcicCallbackHandlingStrategy {

    private final CmcicCallbackHandlingStrategy handlingStrategy;

    public UnexpectedDataLogging(CmcicCallbackHandlingStrategy handlingStrategy) {
        this.handlingStrategy = handlingStrategy;
    }

    @Override
    public void handleCallback(CmcicCallbackData cmcicCallbackData) throws PaymentException {
        if (!cmcicCallbackData.getUnexpectedCallbackData().isEmpty()) {
            log.warn(
                    "Callback contains unexpected data: {}",
                    cmcicCallbackData.getUnexpectedCallbackData());
        }
        handlingStrategy.handleCallback(cmcicCallbackData);
    }
}
