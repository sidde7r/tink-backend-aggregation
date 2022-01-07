package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static se.tink.libraries.signableoperation.enums.InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

@Slf4j
public class CmcicCallbackUnknownHandler implements CmcicCallbackHandlingStrategy {

    @Override
    public void handleCallback(CmcicCallbackData cmcicCallbackData) {
        Map<String, String> unexpectedCallbackData = cmcicCallbackData.getUnexpectedCallbackData();
        log.error(
                "Unknown callback data was received. Status: {}, data: {}",
                cmcicCallbackData.getStatus(),
                unexpectedCallbackData);
        throw new PaymentException(BANK_ERROR_CODE_NOT_HANDLED_YET); // Is this good exception?
    }
}
