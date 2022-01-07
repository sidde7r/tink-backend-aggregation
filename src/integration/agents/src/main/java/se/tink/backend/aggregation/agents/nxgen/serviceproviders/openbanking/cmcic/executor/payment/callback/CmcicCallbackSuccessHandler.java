package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.STATE;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRepository;

@Slf4j
public class CmcicCallbackSuccessHandler implements CmcicCallbackHandlingStrategy {

    private final CmcicRepository cmcicRepository;

    public CmcicCallbackSuccessHandler(CmcicRepository cmcicRepository) {
        this.cmcicRepository = cmcicRepository;
    }

    @Override
    public void handleCallback(CmcicCallbackData cmcicCallbackData) {
        Map<String, String> expectedCallbackData = cmcicCallbackData.getExpectedCallbackData();
        String code = expectedCallbackData.get(CODE);
        String state = expectedCallbackData.get(STATE);
        log.info("Successful payment callback received with code: {}, state: {}", code, state);
        cmcicRepository.storeAuthorizationCode(code);
    }
}
