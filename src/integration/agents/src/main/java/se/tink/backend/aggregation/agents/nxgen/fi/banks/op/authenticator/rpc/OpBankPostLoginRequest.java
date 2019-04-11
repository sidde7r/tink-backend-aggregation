package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPostLoginRequest {
    private String appGroup = OpBankConstants.LoginConstants.GROUP_ID;
    private String touchIdEnabled = OpBankConstants.LoginConstants.TOUCH_ID_ENABLED;
    private String applicationInstanceId = "";

    public OpBankPostLoginRequest setApplicationInstanceId(String appid) {
        this.applicationInstanceId = appid;
        return this;
    }
}
