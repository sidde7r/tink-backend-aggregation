package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import java.util.Map;
import lombok.Getter;

@Getter
public class CmcicCallbackData {

    private final CmcicCallbackStatus status;
    private final Map<String, String> expectedCallbackData;
    private final Map<String, String> unexpectedCallbackData;

    public CmcicCallbackData(
            CmcicCallbackStatus status,
            Map<String, String> expectedCallbackData,
            Map<String, String> unexpectedCallbackData) {
        this.status = status;
        this.expectedCallbackData = expectedCallbackData;
        this.unexpectedCallbackData = unexpectedCallbackData;
    }
}
