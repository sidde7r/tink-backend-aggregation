package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.DefaultRequestParams;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionRequest {
    private String language;
    private String origin;
    private String channel;
    private String instalationId;
    private boolean virtualKeyboard;
    private boolean deferredPushAutAdapted;
    private String operatingSystem;

    public SessionRequest() {
        this.origin = ImaginBankConstants.DefaultRequestParams.ORIGIN;
        this.channel = ImaginBankConstants.DefaultRequestParams.CHANNEL;
        this.deferredPushAutAdapted =
                ImaginBankConstants.DefaultRequestParams.DEFERRED_PUSH_AUT_ADAPTER;
        this.instalationId = DefaultRequestParams.INSTALATION_ID;
        this.language = ImaginBankConstants.DefaultRequestParams.LANGUAGE_EN;
        this.operatingSystem = DefaultRequestParams.OPERATING_SYSTEM;
        this.virtualKeyboard = ImaginBankConstants.DefaultRequestParams.VIRTUAL_KEYBOARD;
    }
}
