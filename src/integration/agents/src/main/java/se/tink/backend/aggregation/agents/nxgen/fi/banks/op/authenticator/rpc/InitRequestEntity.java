package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitRequestEntity {

    private String osVersion = OpBankConstants.Init.OS_VERSION;
    private String os = OpBankConstants.Init.OS_NAME;
    private boolean rooted = OpBankConstants.Init.ROOTED;
    private String appVersion = OpBankConstants.Init.APP_VERSION;
    private String hwType = OpBankConstants.Init.HW_TYPE;
}
