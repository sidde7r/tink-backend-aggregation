package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankLoginRequestEntity {
    private String password;
    private String core = OpBankConstants.Init.CORE;
    private String userid;
    private String lang = OpBankConstants.LANGUAGE_CODE;
    private String applicationGroupId = OpBankConstants.Init.APPLICATION_GROUP_ID;
    private String applicationInstanceId;

    public OpBankLoginRequestEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    public OpBankLoginRequestEntity setUserid(String userid) {
        this.userid = userid;
        return this;
    }

    public OpBankLoginRequestEntity setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
        return this;
    }
}
