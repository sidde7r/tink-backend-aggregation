package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public interface CmcicCallbackHandlingStrategy {

    void handleCallback(CmcicCallbackData cmcicCallbackData) throws PaymentException;
}
